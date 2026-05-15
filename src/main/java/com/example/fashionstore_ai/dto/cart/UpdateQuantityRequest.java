package com.example.fashionstore_ai.dto.cart;


import jakarta.validation.constraints.Min;

public record UpdateQuantityRequest(
        @Min(0) int quantity
) {}
