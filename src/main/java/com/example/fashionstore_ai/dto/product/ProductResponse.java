package com.example.fashionstore_ai.dto.product;


import com.example.fashionstore_ai.enums.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ProductResponse(

        Long id,
        String name,
        String description,
        String brand,
        String sku,
        BigDecimal price,
        BigDecimal discountedPrice,   // price з урахуванням знижки
        Integer discountPercent,
        Category category,
        Gender gender,
        Season season,
        Color color,
        String colorDescription,
        Material material,
        String fabricComposition,
        FitType fitType,
        String careInstructions,
        String countryOfOrigin,
        Boolean isNew,
        Boolean isBestseller,
        String styleNotes,
        String imageUrl,
        List<String> tags,

        // розміри і наявність — Map<Size, stockQuantity>
        Map<Size, Integer> availableSizes
) {}
