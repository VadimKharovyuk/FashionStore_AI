package com.example.fashionstore_ai.dto.order;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReturnRequest(

        @NotNull(message = "itemId обов'язковий")
        Long itemId,

        @NotBlank(message = "Причина повернення обов'язкова")
        String reason
) {}
