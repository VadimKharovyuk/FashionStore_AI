package com.example.fashionstore_ai.dto.order;


import com.example.fashionstore_ai.enums.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderItemResponse(

        Long id,
        Long productId,
        String productName,
        String brand,
        String imageUrl,
        Size size,
        Integer quantity,
        BigDecimal priceAtOrder,
        BigDecimal subtotal,
        String returnStatus,     // null / REQUESTED / COMPLETED
        String returnReason,
        LocalDateTime returnRequestedAt
) {}