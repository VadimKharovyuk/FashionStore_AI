package com.example.fashionstore_ai.service;
import com.example.fashionstore_ai.dto.order.CreateOrderRequest;
import com.example.fashionstore_ai.dto.order.OrderResponse;
import com.example.fashionstore_ai.enums.OrderStatus;
import com.example.fashionstore_ai.mapper.OrderMapper;
import com.example.fashionstore_ai.model.*;
import com.example.fashionstore_ai.repository.*;
import com.example.fashionstore_ai.service.CartService;
import com.example.fashionstore_ai.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    private final OrderMapper orderMapper;

    private static final AtomicLong orderCounter = new AtomicLong(1000);

    // ── createFromCart ────────────────────────────────────────────

    @Override
    @Transactional
    public OrderResponse createFromCart(String sessionId, CreateOrderRequest request) {
        Cart cart = cartRepository.findBySessionIdWithItems(sessionId)
                .orElseThrow(() -> new IllegalStateException("Кошик порожній"));

        if (cart.isEmpty()) {
            throw new IllegalStateException("Кошик порожній — нічого оформляти");
        }

        Order order = Order.builder()
                .sessionId(sessionId)
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING)
                .deliveryAddress(request.deliveryAddress())
                .recipientName(request.recipientName())
                .recipientPhone(request.recipientPhone())
                .recipientEmail(request.recipientEmail())
                .estimatedDelivery(LocalDate.now().plusDays(7))
                .build();

        Order saved = orderRepository.save(order);

        // переносимо items з кошика в замовлення
        cart.getItems().forEach(cartItem -> {
            OrderItem orderItem = OrderItem.builder()
                    .order(saved)
                    .product(cartItem.getProduct())
                    .size(cartItem.getSize())
                    .quantity(cartItem.getQuantity())
                    .priceAtOrder(cartItem.getPriceAtAdd())
                    .build();
            orderItemRepository.save(orderItem);
        });

        // очищаємо кошик
        cartService.clearCart(sessionId);

        log.info("OrderService: створено замовлення {} для sessionId={}",
                saved.getOrderNumber(), sessionId);

        return orderMapper.toResponse(
                orderRepository.findByOrderNumberAndSessionIdWithItems(
                        saved.getOrderNumber(), sessionId).orElse(saved));
    }

    // ── getByOrderNumber ──────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getByOrderNumber(String orderNumber, String sessionId) {
        Order order = orderRepository
                .findByOrderNumberAndSessionIdWithItems(orderNumber, sessionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Замовлення " + orderNumber + " не знайдено"));
        return orderMapper.toResponse(order);
    }

    // ── getAllBySession ────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllBySession(String sessionId) {
        return orderRepository.findBySessionIdOrderByCreatedAtDesc(sessionId)
                .stream()
                .map(orderMapper::toResponseLight)
                .toList();
    }

    // ── cancelOrder ───────────────────────────────────────────────

    @Override
    @Transactional
    public OrderResponse cancelOrder(String orderNumber, String sessionId) {
        Order order = findOrder(orderNumber, sessionId);

        if (!order.isCancellable()) {
            throw new IllegalStateException(
                    "Замовлення " + orderNumber + " не можна скасувати. " +
                            "Статус: " + order.getStatus() + ". " +
                            "Скасування доступне тільки для PENDING і CONFIRMED.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info("OrderService: скасовано замовлення {} для sessionId={}",
                orderNumber, sessionId);

        return orderMapper.toResponse(order);
    }

    // ── updateDeliveryAddress ─────────────────────────────────────

    @Override
    @Transactional
    public OrderResponse updateDeliveryAddress(String orderNumber, String sessionId,
                                               String newAddress) {
        Order order = findOrder(orderNumber, sessionId);

        if (!order.isAddressChangeable()) {
            throw new IllegalStateException(
                    "Адресу замовлення " + orderNumber + " вже не можна змінити. " +
                            "Статус: " + order.getStatus());
        }

        order.setDeliveryAddress(newAddress);
        orderRepository.save(order);

        log.info("OrderService: змінено адресу замовлення {} для sessionId={}",
                orderNumber, sessionId);

        return orderMapper.toResponse(order);
    }

    // ── createReturnRequest ───────────────────────────────────────

    @Override
    @Transactional
    public OrderResponse createReturnRequest(String orderNumber, String sessionId,
                                             Long itemId, String reason) {
        Order order = findOrder(orderNumber, sessionId);

        if (!order.isReturnable()) {
            throw new IllegalStateException(
                    "Повернення для замовлення " + orderNumber + " недоступне. " +
                            "Повернення можливе тільки для доставлених замовлень (DELIVERED).");
        }

        OrderItem item = orderItemRepository
                .findByIdAndOrderId(itemId, order.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Позиція " + itemId + " не знайдена в замовленні " + orderNumber));

        if (item.hasReturnRequest()) {
            throw new IllegalStateException(
                    "Запит на повернення вже існує для цієї позиції. " +
                            "Статус: " + item.getReturnStatus());
        }

        item.setReturnStatus("REQUESTED");
        item.setReturnReason(reason);
        item.setReturnRequestedAt(LocalDateTime.now());
        orderItemRepository.save(item);

        order.setNotes("Запит на повернення: " + reason);
        orderRepository.save(order);

        log.info("OrderService: запит на повернення для {} itemId={} sessionId={}",
                orderNumber, itemId, sessionId);

        return orderMapper.toResponse(order);
    }

    // ── getTrackingInfo ───────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public String getTrackingInfo(String orderNumber, String sessionId) {
        Order order = findOrder(orderNumber, sessionId);

        if (order.getTrackingNumber() == null) {
            return switch (order.getStatus()) {
                case PENDING   -> "Замовлення " + orderNumber + " очікує підтвердження.";
                case CONFIRMED -> "Замовлення підтверджено, готується до відправки.";
                case CANCELLED -> "Замовлення скасовано.";
                default        -> "Трекінг-номер ще не призначено.";
            };
        }

        String info = "Замовлення: " + orderNumber + "\n" +
                "Трекінг: " + order.getTrackingNumber() + "\n" +
                "Статус: " + order.getStatus();

        if (order.getEstimatedDelivery() != null) {
            info += "\nОчікувана доставка: " + order.getEstimatedDelivery();
        }

        return info;
    }

    // ── generateOrderNumber ───────────────────────────────────────

    @Override
    public String generateOrderNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        String seq  = String.format("%06d", orderCounter.getAndIncrement());
        String number = "ORD-" + year + "-" + seq;

        // якщо вже існує — генеруємо новий
        while (orderRepository.existsByOrderNumber(number)) {
            seq    = String.format("%06d", orderCounter.getAndIncrement());
            number = "ORD-" + year + "-" + seq;
        }
        return number;
    }

    // ── Private helpers ───────────────────────────────────────────

    private Order findOrder(String orderNumber, String sessionId) {
        return orderRepository
                .findByOrderNumberAndSessionId(orderNumber, sessionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Замовлення " + orderNumber + " не знайдено або не належить вашій сесії"));
    }
}
