package com.example.fashionstore_ai.dto.order;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(

        @NotBlank(message = "Адреса доставки обов'язкова")
        String deliveryAddress,

        @NotBlank(message = "Ім'я отримувача обов'язкове")
        String recipientName,

        @NotBlank(message = "Телефон обов'язковий")
        String recipientPhone,

        @Email(message = "Невірний формат email")
        String recipientEmail
) {}