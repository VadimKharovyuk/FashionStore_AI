package com.example.fashionstore_ai.dto.userMeasurement;


import com.example.fashionstore_ai.enums.Size;

public record SizeRecommendation(
        String brand,
        Size recommendedSize,     // основна рекомендація
        Size alternativeSize,     // альтернатива (якщо на межі)
        String explanation,       // детальне пояснення з цифрами
        String fitNotes,          // нотатки бренду
        boolean isOnBorder,       // користувач на межі двох розмірів
        String confidence         // HIGH / MEDIUM / LOW
) {}
