package com.example.fashionstore_ai.service.impl;
import com.example.fashionstore_ai.dto.product.ProductResponse;
import com.example.fashionstore_ai.enums.*;
import com.example.fashionstore_ai.mapper.ProductMapper;
import com.example.fashionstore_ai.model.Product;
import com.example.fashionstore_ai.model.ViewHistory;
import com.example.fashionstore_ai.repository.ProductRepository;
import com.example.fashionstore_ai.repository.ProductSizeRepository;
import com.example.fashionstore_ai.repository.ViewHistoryRepository;
import com.example.fashionstore_ai.service.ProductService;
import com.example.fashionstore_ai.util.PageResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;
    private final ViewHistoryRepository viewHistoryRepository;
    private final ProductMapper productMapper;

    // ── getById ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Товар не знайдено: id=" + id));
        return productMapper.toResponse(product);
    }

    // ── search ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> search(String name,
                                        Category category,
                                        Gender gender,
                                        Season season,
                                        Color color,
                                        Material material,
                                        FitType fitType,
                                        BigDecimal maxPrice) {

        log.info("ProductService.search: name='{}' category={} gender={} season={} color={} material={} fitType={} maxPrice={}",
                name, category, gender, season, color, material, fitType, maxPrice);

        List<Product> products = productRepository.findWithFilters(
                name,
                category != null ? category.name() : null,
                gender != null ? gender.name() : null,
                season != null ? season.name() : null,
                color != null ? color.name() : null,
                material != null ? material.name() : null,
                fitType != null ? fitType.name() : null,
                maxPrice);

        log.info("ProductService.search: знайдено {} товарів для name='{}'", products.size(), name);

        if (products.isEmpty()) return Collections.emptyList();
        return productMapper.toResponseList(products);
    }

    // ── checkStock ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public int checkStock(Long productId, Size size) {
        return productSizeRepository
                .findByProductIdAndSize(productId, size)
                .map(ps -> ps.getStockQuantity())
                .orElse(0);
    }

    // ── RecommendationAgent ───────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getBestsellers(Category category, Gender gender) {
        List<Product> all = productRepository.findByIsBestsellerTrueOrderByCreatedAtDesc();

        List<Product> filtered = all.stream()
                .filter(p -> category == null || p.getCategory() == category)
                .filter(p -> gender == null || p.getGender() == gender
                        || p.getGender() == Gender.UNISEX)
                .toList();

        return productMapper.toResponseList(filtered);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getNewArrivals(Category category, Gender gender) {
        List<Product> all = productRepository.findByIsNewTrueOrderByCreatedAtDesc();

        List<Product> filtered = all.stream()
                .filter(p -> category == null || p.getCategory() == category)
                .filter(p -> gender == null || p.getGender() == gender
                        || p.getGender() == Gender.UNISEX)
                .toList();

        return productMapper.toResponseList(filtered);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getSimilar(Long productId, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Товар не знайдено: id=" + productId));

        List<Product> similar = productRepository.findSimilar(
                product.getCategory(), productId, limit);

        return productMapper.toResponseList(similar);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getByTags(List<String> tags, List<Long> excludeIds) {
        if (tags == null || tags.isEmpty()) return Collections.emptyList();

        List<Long> exclude = (excludeIds == null || excludeIds.isEmpty())
                ? List.of(-1L)  // фіктивний id щоб NOT IN не був порожнім
                : excludeIds;

        List<Product> products = productRepository.findByTagsIn(tags, exclude);
        return productMapper.toResponseList(products);
    }

    // ── recordView ────────────────────────────────────────────────

    @Override
    @Transactional
    public void recordView(String sessionId, Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return;

        viewHistoryRepository
                .findBySessionIdAndProductId(sessionId, productId)
                .ifPresentOrElse(
                        vh -> {
                            vh.setViewCount(vh.getViewCount() + 1);
                            vh.setLastViewedAt(LocalDateTime.now());
                            viewHistoryRepository.save(vh);
                        },
                        () -> viewHistoryRepository.save(
                                ViewHistory.builder()
                                        .sessionId(sessionId)
                                        .product(product)
                                        .viewCount(1)
                                        .build()
                        )
                );

        log.debug("ProductService.recordView: sessionId={} productId={}", sessionId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getCatalog(Category category,
                                                    Gender gender,
                                                    Season season,
                                                    Color color,
                                                    Material material,
                                                    FitType fitType,
                                                    BigDecimal maxPrice,
                                                    String search,
                                                    int page,
                                                    int size) {
        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by(Sort.Order.desc("isBestseller"), Sort.Order.desc("createdAt"))
        );

        Page<Product> productPage = productRepository.findWithFiltersPageable(
                category, gender, season, color, material, fitType, maxPrice, pageable
        );

        // фільтр по search в Java — уникаємо проблеми з bytea в JPQL
        String term = (search != null && !search.isBlank()) ? search.trim().toLowerCase() : null;

        Page<ProductResponse> responsePage = productPage.map(p -> {
            if (term != null) {
                boolean matches = p.getName().toLowerCase().contains(term)
                        || (p.getBrand() != null && p.getBrand().toLowerCase().contains(term));
                if (!matches) return null;
            }
            return productMapper.toResponse(p);
        }).map(r -> r); // map не фільтрує null — треба окремо

        // фільтруємо null після map
        List<ProductResponse> filtered = productPage.getContent().stream()
                .filter(p -> term == null
                        || p.getName().toLowerCase().contains(term)
                        || (p.getBrand() != null && p.getBrand().toLowerCase().contains(term)))
                .map(productMapper::toResponse)
                .toList();

        // будуємо PageResponse вручну
        return PageResponse.<ProductResponse>builder()
                .content(filtered)
                .currentPage(productPage.getNumber())
                .totalPages(productPage.getTotalPages())
                .totalElements(productPage.getTotalElements())
                .pageSize(productPage.getSize())
                .hasNext(productPage.hasNext())
                .hasPrevious(productPage.hasPrevious())
                .isFirst(productPage.isFirst())
                .isLast(productPage.isLast())
                .build();
    }
}