package com.example.fashionstore_ai.dto.chat;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(

        @NotBlank(message = "Повідомлення не може бути порожнім")
        @Size(max = 2000, message = "Повідомлення не може перевищувати 2000 символів")
        String message,

        // sessionId береться з cookie в контролері, але можна передати явно
        String sessionId
) {}
