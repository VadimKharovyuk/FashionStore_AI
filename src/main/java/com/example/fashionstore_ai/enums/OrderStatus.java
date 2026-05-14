package com.example.fashionstore_ai.enums;

public enum OrderStatus {
    PENDING,           // щойно створено
    CONFIRMED,         // підтверджено
    SHIPPED,           // відправлено
    IN_TRANSIT,        // в дорозі
    DELIVERED,         // доставлено
    CANCELLED,         // скасовано
    RETURN_REQUESTED,  // запит на повернення
    RETURNED,          // повернення завершено
    EXCHANGED          // обміняно
}
