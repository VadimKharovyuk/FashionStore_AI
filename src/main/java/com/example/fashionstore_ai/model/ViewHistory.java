package com.example.fashionstore_ai.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "view_history", indexes = {
        @Index(name = "idx_view_history_session_id",  columnList = "session_id"),
        @Index(name = "idx_view_history_product_id",  columnList = "product_id"),
        @Index(name = "idx_view_history_last_viewed", columnList = "last_viewed_at")
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // прив'язка до сесії — без авторизації
    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    // який товар переглядали
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // скільки разів переглядали — чим більше, тим вищий інтерес
    // RecommendationAgent зважує рекомендації по цьому полю
    @Column(name = "view_count")
    private Integer viewCount = 1;

    @Column(name = "last_viewed_at")
    private LocalDateTime lastViewedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastViewedAt = LocalDateTime.now();
    }
}