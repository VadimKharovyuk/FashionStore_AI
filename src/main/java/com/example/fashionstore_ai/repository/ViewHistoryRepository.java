package com.example.fashionstore_ai.repository;

import com.example.fashionstore_ai.model.ViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {

    // перевірити чи вже є запис (щоб інкрементувати viewCount а не дублювати)
    Optional<ViewHistory> findBySessionIdAndProductId(String sessionId, Long productId);

    // топ переглянутих товарів по сесії — для RecommendationAgent
    // сортуємо по viewCount DESC щоб найцікавіші були першими
    @Query("""
            SELECT v FROM ViewHistory v
            WHERE v.sessionId = :sessionId
            ORDER BY v.viewCount DESC, v.lastViewedAt DESC
            LIMIT :limit
            """)
    List<ViewHistory> findTopBySessionId(@Param("sessionId") String sessionId,
                                         @Param("limit") int limit);

    // всі перегляди сесії (для аналізу категорій і кольорів)
    List<ViewHistory> findBySessionIdOrderByLastViewedAtDesc(String sessionId);

    // товари які переглядали більше N разів — сильний інтерес
    @Query("""
            SELECT v FROM ViewHistory v
            WHERE v.sessionId = :sessionId
              AND v.viewCount >= :minCount
            ORDER BY v.viewCount DESC
            """)
    List<ViewHistory> findBySessionIdAndViewCountGreaterThan(
            @Param("sessionId") String sessionId,
            @Param("minCount") int minCount);
}