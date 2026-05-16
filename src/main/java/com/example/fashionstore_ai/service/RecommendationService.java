package com.example.fashionstore_ai.service;

import com.example.fashionstore_ai.dto.product.ProductResponse;
import com.example.fashionstore_ai.enums.Category;
import com.example.fashionstore_ai.enums.Gender;

import java.util.List;

public interface RecommendationService {

    // персональні рекомендації на основі ViewHistory
    List<ProductResponse> getPersonalized(String sessionId, int limit);

    List<ProductResponse> getPersonalized(String sessionId, Gender gender, int limit);

    // схожі товари (та сама категорія)
    List<ProductResponse> getSimilar(Long productId, int limit);

    // доповнюючі товари (інші категорії що підходять разом)
    List<ProductResponse> getComplementary(List<Long> productIds, int limit);

    // по тегах (виключаючи вже переглянуті)
    List<ProductResponse> getByTags(List<String> tags, List<Long> excludeIds);


    List<ProductResponse> getByTags(List<String> tags, List<Long> excludeIds, Gender gender);

    // хіти продажів
    List<ProductResponse> getBestsellers(Category category, Gender gender, int limit);

    // новинки
    List<ProductResponse> getNewArrivals(Category category, Gender gender, int limit);

    // історія переглядів
    List<ProductResponse> getViewHistory(String sessionId, int limit);
}
