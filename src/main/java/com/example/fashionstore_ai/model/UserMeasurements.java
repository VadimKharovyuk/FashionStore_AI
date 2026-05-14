package com.example.fashionstore_ai.model;

import com.example.fashionstore_ai.enums.FitType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
///UserMeasurements — це сутність де зберігаються параметри
/// тіла користувача для SizingAgent.
@Entity
@Table(name = "user_measurements", indexes = {
        @Index(name = "idx_user_measurements_session_id", columnList = "session_id")
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class UserMeasurements {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // прив'язка до сесії — без авторизації
    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    // ── Параметри тіла (см) ───────────────────────────────────────
    @Column(name = "chest")
    private Integer chest;       // обхват грудей

    @Column(name = "waist")
    private Integer waist;       // обхват талії

    @Column(name = "hips")
    private Integer hips;        // обхват стегон

    @Column(name = "height")
    private Integer height;      // зріст

    @Column(name = "weight")
    private Integer weight;      // вага кг — опціонально, деякі не хочуть вводити

    // ── Уподобання посадки ────────────────────────────────────────
    // SizingAgent враховує при рекомендації:
    // SLIM    → якщо на межі — радить менший розмір
    // REGULAR → рекомендує точно по сітці
    // OVERSIZE → якщо на межі — радить більший розмір
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_fit", length = 50)
    private FitType preferredFit = FitType.REGULAR;

    // ── Timestamps ────────────────────────────────────────────────
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Helper ────────────────────────────────────────────────────
    // SizingAgent перевіряє чи достатньо даних для порівняння з сіткою
    // мінімум потрібно хоча б одне з трьох: груди / талія / стегна
    public boolean hasEnoughData() {
        return chest != null || waist != null || hips != null;
    }

    // форматований рядок для передачі в LLM контекст
    public String toPromptString() {
        StringBuilder sb = new StringBuilder("Параметри користувача: ");
        if (chest  != null) sb.append("груди ").append(chest).append("см, ");
        if (waist  != null) sb.append("талія ").append(waist).append("см, ");
        if (hips   != null) sb.append("стегна ").append(hips).append("см, ");
        if (height != null) sb.append("зріст ").append(height).append("см, ");
        if (weight != null) sb.append("вага ").append(weight).append("кг, ");
        sb.append("бажана посадка: ").append(preferredFit);
        return sb.toString();
    }
}