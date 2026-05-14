package com.example.fashionstore_ai.dto.cart;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CartResponse(

        Long id,
        String sessionId,
        List<CartItemResponse> items,
        int totalItems,           // загальна кількість одиниць
        BigDecimal totalPrice,    // сума всього кошика
        boolean isEmpty,
        LocalDateTime updatedAt
) {}
