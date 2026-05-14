package com.example.fashionstore_ai.mapper;

import com.example.fashionstore_ai.dto.order.OrderItemResponse;
import com.example.fashionstore_ai.dto.order.OrderResponse;
import com.example.fashionstore_ai.model.Order;
import com.example.fashionstore_ai.model.OrderItem;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class OrderMapper {

    // ── OrderItem ─────────────────────────────────────────────────

    public OrderItemResponse toResponse(OrderItem item) {
        if (item == null) return null;

        return new OrderItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getBrand(),
                item.getProduct().getImageUrl(),
                item.getSize(),
                item.getQuantity(),
                item.getPriceAtOrder(),
                item.getSubtotal(),
                item.getReturnStatus(),
                item.getReturnReason(),
                item.getReturnRequestedAt()
        );
    }

    public List<OrderItemResponse> toResponseList(List<OrderItem> items) {
        if (items == null || items.isEmpty()) return Collections.emptyList();
        return items.stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Order ─────────────────────────────────────────────────────

    public OrderResponse toResponse(Order order) {
        if (order == null) return null;

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                toResponseList(order.getItems()),
                order.getTotalItems(),
                order.getTotalPrice(),
                order.getDeliveryAddress(),
                order.getRecipientName(),
                order.getRecipientPhone(),
                order.getRecipientEmail(),
                order.getTrackingNumber(),
                order.getEstimatedDelivery(),
                order.isCancellable(),
                order.isAddressChangeable(),
                order.isReturnable(),
                order.getNotes(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    // ── Order без items (для списку замовлень) ────────────────────

    public OrderResponse toResponseLight(Order order) {
        if (order == null) return null;

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                Collections.emptyList(),
                order.getTotalItems(),
                order.getTotalPrice(),
                order.getDeliveryAddress(),
                order.getRecipientName(),
                order.getRecipientPhone(),
                order.getRecipientEmail(),
                order.getTrackingNumber(),
                order.getEstimatedDelivery(),
                order.isCancellable(),
                order.isAddressChangeable(),
                order.isReturnable(),
                order.getNotes(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
