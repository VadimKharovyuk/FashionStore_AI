package com.example.fashionstore_ai.model;

import com.example.fashionstore_ai.enums.AgentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "chat_session", indexes = {
        @Index(name = "idx_chat_session_session_id", columnList = "session_id"),
        @Index(name = "idx_chat_session_status",     columnList = "status")
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // той самий UUID що і в Cart / Order — ключ ідентифікації без авторизації
    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    // який агент зараз активний
    @Enumerated(EnumType.STRING)
    @Column(name = "active_agent_type", length = 50)
    private AgentType activeAgentType;

    @Column(name = "status", length = 20)
    private String status = "ACTIVE"; // ACTIVE / CLOSED

    // ── Рівень 3: Rolling Summary ─────────────────────────────────
    // Стиснутий текст старих повідомлень (> 30 в history)
    // Зберігається як SystemMessage на початку контексту
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    // ── Critical Facts ─────────────────────────────────────────────
// Структуровані факти що не дрейфують на відміну від summary
    @Column(name = "critical_facts", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> criticalFacts;

    @Column(name = "summary_updated_at")
    private LocalDateTime summaryUpdatedAt;

    // скільки повідомлень вже стиснуто в summary
    // потрібно щоб знати з якого індексу починати sliding window
    @Column(name = "summarized_messages_count")
    private Integer summarizedMessagesCount = 0;


    // ── Повідомлення ──────────────────────────────────────────────
    @OneToMany(mappedBy = "chatSession",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

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
}