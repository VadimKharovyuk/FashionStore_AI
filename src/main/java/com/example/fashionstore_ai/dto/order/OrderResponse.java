package com.example.fashionstore_ai.dto.order;
import com.example.fashionstore_ai.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(

        Long id,
        String orderNumber,
        OrderStatus status,
        List<OrderItemResponse> items,
        int totalItems,
        BigDecimal totalPrice,

        // доставка
        String deliveryAddress,
        String recipientName,
        String recipientPhone,
        String recipientEmail,
        String trackingNumber,
        LocalDate estimatedDelivery,

        // можливі дії — SupportAgent використовує щоб знати що пропонувати
        boolean isCancellable,
        boolean isAddressChangeable,
        boolean isReturnable,

        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
