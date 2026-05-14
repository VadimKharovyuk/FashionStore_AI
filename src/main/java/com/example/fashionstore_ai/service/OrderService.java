package com.example.fashionstore_ai.service;


import com.example.fashionstore_ai.dto.order.CreateOrderRequest;
import com.example.fashionstore_ai.dto.order.OrderResponse;
import com.example.fashionstore_ai.enums.OrderStatus;

import java.util.List;

public interface OrderService {

    // створити замовлення з кошика
    OrderResponse createFromCart(String sessionId, CreateOrderRequest request);

    // отримати замовлення по номеру (тільки своє — sessionId обов'язковий)
    OrderResponse getByOrderNumber(String orderNumber, String sessionId);

    // всі замовлення сесії
    List<OrderResponse> getAllBySession(String sessionId);

    // SupportAgent: скасувати замовлення
    OrderResponse cancelOrder(String orderNumber, String sessionId);

    // SupportAgent: змінити адресу доставки
    OrderResponse updateDeliveryAddress(String orderNumber, String sessionId, String newAddress);

    // SupportAgent: створити запит на повернення конкретного item
    OrderResponse createReturnRequest(String orderNumber, String sessionId,
                                      Long itemId, String reason);

    // SupportAgent: отримати статус трекінгу
    String getTrackingInfo(String orderNumber, String sessionId);

    // генерація унікального номера замовлення
    String generateOrderNumber();
}
