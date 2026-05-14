package com.example.fashionstore_ai.model;

import com.example.fashionstore_ai.enums.AgentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message", indexes = {
        @Index(name = "idx_chat_message_session", columnList = "chat_session_id"),
        @Index(name = "idx_chat_message_created", columnList = "created_at")
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_session_id", nullable = false)
    private ChatSession chatSession;

    // "user" / "assistant"
    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // який агент відповів (null для user повідомлень)
    @Enumerated(EnumType.STRING)
    @Column(name = "agent_type", length = 50)
    private AgentType agentType;

    // ── Для Pinned Context (Рівень 2) ─────────────────────────────
    // перші 2-3 повідомлення позначаємо як pinned — вони завжди
    // потрапляють в контекст незалежно від sliding window
    @Column(name = "is_pinned")
    private Boolean isPinned = false;

    // порядковий номер в сесії — для зручного pinned/window слайсингу
    @Column(name = "message_index")
    private Integer messageIndex;

    // ── Debug ─────────────────────────────────────────────────────
    // JSON лог tool викликів для дебагу — не йде в контекст LLM
    @Column(name = "tool_calls_json", columnDefinition = "TEXT")
    private String toolCallsJson;

    // скільки токенів зайняло це повідомлення (опціонально)
    @Column(name = "token_count")
    private Integer tokenCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}