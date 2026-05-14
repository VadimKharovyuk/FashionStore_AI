package com.example.fashionstore_ai.service;


import com.example.fashionstore_ai.dto.product.ProductResponse;
import com.example.fashionstore_ai.enums.*;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    // отримати товар по id
    ProductResponse getById(Long id);

    // пошук з фільтрами — всі параметри опціональні (null = не фільтрувати)
    // використовується ShoppingAssistant через ProductSearchTool
    List<ProductResponse> search(Category category,
                                 Gender gender,
                                 Season season,
                                 Color color,
                                 Material material,
                                 FitType fitType,
                                 BigDecimal maxPrice);

    // перевірити наявність конкретного розміру
    // повертає кількість на складі (0 = немає)
    int checkStock(Long productId, Size size);

    // RecommendationAgent
    List<ProductResponse> getBestsellers(Category category, Gender gender);
    List<ProductResponse> getNewArrivals(Category category, Gender gender);
    List<ProductResponse> getSimilar(Long productId, int limit);
    List<ProductResponse> getByTags(List<String> tags, List<Long> excludeIds);

    // записати перегляд товару (для ViewHistory)
    void recordView(String sessionId, Long productId);
}
