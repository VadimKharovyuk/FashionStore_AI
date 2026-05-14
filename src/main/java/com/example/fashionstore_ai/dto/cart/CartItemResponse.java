package com.example.fashionstore_ai.dto.cart;


import com.example.fashionstore_ai.enums.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CartItemResponse(

        Long id,
        Long productId,
        String productName,
        String brand,
        String imageUrl,
        Size size,
        Integer quantity,
        BigDecimal priceAtAdd,   // ціна за одиницю
        BigDecimal subtotal,     // priceAtAdd * quantity
        LocalDateTime createdAt
) {}
