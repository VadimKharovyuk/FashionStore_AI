package com.example.fashionstore_ai.dto.cart;


import com.example.fashionstore_ai.enums.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemRequest(

        @NotNull(message = "productId обов'язковий")
        Long productId,

        @NotNull(message = "розмір обов'язковий")
        Size size,

        @Min(value = 1, message = "кількість має бути більше 0")
        int quantity
) {}
