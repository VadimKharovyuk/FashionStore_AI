package com.example.fashionstore_ai.service;

import com.example.fashionstore_ai.dto.product.ProductResponse;
import com.example.fashionstore_ai.enums.Category;
import com.example.fashionstore_ai.enums.Gender;
import com.example.fashionstore_ai.mapper.ProductMapper;
import com.example.fashionstore_ai.model.Product;
import com.example.fashionstore_ai.model.ViewHistory;
import com.example.fashionstore_ai.repository.ProductRepository;
import com.example.fashionstore_ai.repository.ViewHistoryRepository;
import com.example.fashionstore_ai.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final ViewHistoryRepository viewHistoryRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    // ── getPersonalized ───────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getPersonalized(String sessionId, int limit) {
        List<ViewHistory> history = viewHistoryRepository
                .findTopBySessionId(sessionId, 10);

        if (history.isEmpty()) {
            // якщо немає history — повертаємо bestsellers
            log.debug("RecommendationService: немає history для sessionId={}, повертаємо bestsellers", sessionId);
            return getBestsellers(null, null, limit);
        }

        // збираємо теги і категорії з переглянутих товарів
        Set<String> tags       = new LinkedHashSet<>();
        Set<Category> categories = new LinkedHashSet<>();
        Set<Long> excludeIds   = new HashSet<>();

        history.forEach(vh -> {
            Product p = vh.getProduct();
            excludeIds.add(p.getId());
            if (p.getTags() != null) tags.addAll(p.getTags());
            if (p.getCategory() != null) categories.add(p.getCategory());
        });

        List<ProductResponse> result = new ArrayList<>();

        // 1. По тегах (найбільш персональні)
        if (!tags.isEmpty()) {
            List<ProductResponse> byTags = getByTags(new ArrayList<>(tags),
                    new ArrayList<>(excludeIds));
            result.addAll(byTags);
        }

        // 2. По категоріях якщо мало результатів
        if (result.size() < limit && !categories.isEmpty()) {
            Category topCategory = categories.iterator().next();
            List<Product> byCat = productRepository
                    .findByCategoryAndGenderAndIdNotIn(topCategory, null,
                            new ArrayList<>(excludeIds));
            byCat.stream()
                    .limit(limit - result.size())
                    .map(productMapper::toResponse)
                    .forEach(result::add);
        }

        log.debug("RecommendationService.getPersonalized: sessionId={} results={}",
                sessionId, result.size());

        return result.stream().limit(limit).toList();
    }

    // ── getSimilar ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getSimilar(Long productId, int limit) {
        return productMapper.toResponseList(
                productRepository.findSimilar(
                        productRepository.findById(productId)
                                .map(p -> p.getCategory())
                                .orElseThrow(),
                        productId, limit));
    }

    // ── getComplementary ──────────────────────────────────────────
    // Логіка: якщо в кошику вечірня сукня → пропонуємо аксесуари/взуття
    // Зараз без ML — просто інші категорії які добре поєднуються

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getComplementary(List<Long> productIds, int limit) {
        if (productIds == null || productIds.isEmpty()) return List.of();

        // визначаємо категорії товарів що вже є
        Set<Category> existingCategories = productIds.stream()
                .map(id -> productRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .map(Product::getCategory)
                .collect(Collectors.toSet());

        // complementary map — що підходить до чого
        Map<Category, List<Category>> complementaryMap = Map.of(
                Category.EVENING,    List.of(Category.CLASSIC, Category.BUSINESS),
                Category.CASUAL,     List.of(Category.SPORT, Category.STREETWEAR),
                Category.SPORT,      List.of(Category.CASUAL, Category.LOUNGEWEAR),
                Category.BUSINESS,   List.of(Category.CLASSIC, Category.EVENING),
                Category.BEACH,      List.of(Category.CASUAL, Category.SPORT),
                Category.STREETWEAR, List.of(Category.CASUAL, Category.SPORT),
                Category.LOUNGEWEAR, List.of(Category.CASUAL),
                Category.CLASSIC,    List.of(Category.BUSINESS, Category.EVENING)
        );

        Set<Category> targetCategories = existingCategories.stream()
                .flatMap(cat -> complementaryMap.getOrDefault(cat, List.of()).stream())
                .filter(cat -> !existingCategories.contains(cat))
                .collect(Collectors.toSet());

        if (targetCategories.isEmpty()) return getBestsellers(null, null, limit);

        List<Product> result = new ArrayList<>();
        for (Category cat : targetCategories) {
            productRepository
                    .findByCategoryAndGenderAndIdNotIn(cat, null, productIds)
                    .stream()
                    .limit(2)
                    .forEach(result::add);
            if (result.size() >= limit) break;
        }

        return productMapper.toResponseList(result.stream().limit(limit).toList());
    }

    // ── getByTags ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getByTags(List<String> tags, List<Long> excludeIds) {
        if (tags == null || tags.isEmpty()) return List.of();
        List<Long> exclude = (excludeIds == null || excludeIds.isEmpty())
                ? List.of(-1L) : excludeIds;
        return productMapper.toResponseList(
                productRepository.findByTagsIn(tags, exclude));
    }

    // ── getBestsellers ────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getBestsellers(Category category, Gender gender, int limit) {
        List<Product> all = productRepository.findByIsBestsellerTrueOrderByCreatedAtDesc();
        return all.stream()
                .filter(p -> category == null || p.getCategory() == category)
                .filter(p -> gender == null || p.getGender() == gender
                        || p.getGender() == Gender.UNISEX)
                .limit(limit)
                .map(productMapper::toResponse)
                .toList();
    }

    // ── getNewArrivals ────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getNewArrivals(Category category, Gender gender, int limit) {
        List<Product> all = productRepository.findByIsNewTrueOrderByCreatedAtDesc();
        return all.stream()
                .filter(p -> category == null || p.getCategory() == category)
                .filter(p -> gender == null || p.getGender() == gender
                        || p.getGender() == Gender.UNISEX)
                .limit(limit)
                .map(productMapper::toResponse)
                .toList();
    }

    // ── getViewHistory ────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getViewHistory(String sessionId, int limit) {
        return viewHistoryRepository
                .findTopBySessionId(sessionId, limit)
                .stream()
                .map(vh -> productMapper.toResponse(vh.getProduct()))
                .toList();
    }
}
