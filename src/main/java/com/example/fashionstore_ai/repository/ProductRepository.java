package com.example.fashionstore_ai.repository;


import com.example.fashionstore_ai.enums.*;
import com.example.fashionstore_ai.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    // ── ShoppingAssistant: пошук з фільтрами ─────────────────────
    // всі параметри опціональні — null ігнорується
    @Query("""
            SELECT p FROM Product p
            WHERE (:category IS NULL OR p.category = :category)
              AND (:gender   IS NULL OR p.gender   = :gender)
              AND (:season   IS NULL OR p.season   = :season
                                    OR p.season    = com.example.fashionstore_ai.enums.Season.ALL_SEASON)
              AND (:color    IS NULL OR p.color    = :color)
              AND (:material IS NULL OR p.material = :material)
              AND (:fitType  IS NULL OR p.fitType  = :fitType)
              AND (:maxPrice IS NULL OR p.price    <= :maxPrice)
            ORDER BY p.isBestseller DESC, p.createdAt DESC
            """)
    List<Product> findWithFilters(
            @Param("category") Category category,
            @Param("gender")   Gender gender,
            @Param("season")   Season season,
            @Param("color")    Color color,
            @Param("material") Material material,
            @Param("fitType")  FitType fitType,
            @Param("maxPrice") BigDecimal maxPrice
    );

    // ── RecommendationAgent ───────────────────────────────────────

    List<Product> findByIsBestsellerTrueOrderByCreatedAtDesc();

    List<Product> findByIsNewTrueOrderByCreatedAtDesc();

    List<Product> findByCategoryAndGenderAndIdNotIn(
            Category category, Gender gender, List<Long> excludeIds);

    // пошук по тегах — для getProductsByTags()
    @Query("""
            SELECT DISTINCT p FROM Product p
            JOIN p.tags t
            WHERE t IN :tags
              AND p.id NOT IN :excludeIds
            ORDER BY p.isBestseller DESC
            """)
    List<Product> findByTagsIn(
            @Param("tags")       List<String> tags,
            @Param("excludeIds") List<Long> excludeIds
    );

    // для RecommendationAgent: товари тієї самої категорії (схожі)
    @Query("""
            SELECT p FROM Product p
            WHERE p.category = :category
              AND p.id != :excludeId
            ORDER BY p.isBestseller DESC, p.createdAt DESC
            LIMIT :limit
            """)
    List<Product> findSimilar(
            @Param("category")  Category category,
            @Param("excludeId") Long excludeId,
            @Param("limit")     int limit
    );

    // з sizes одразу — уникаємо N+1 при відображенні каталогу
    @Query("""
            SELECT DISTINCT p FROM Product p
            LEFT JOIN FETCH p.sizes
            WHERE p.id IN :ids
            """)
    List<Product> findByIdsWithSizes(@Param("ids") List<Long> ids);
}
