package com.example.fashionstore_ai.mapper;

import com.example.fashionstore_ai.dto.chat.ChatMessageResponse;
import com.example.fashionstore_ai.dto.chat.ChatSessionResponse;
import com.example.fashionstore_ai.model.ChatMessage;
import com.example.fashionstore_ai.model.ChatSession;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ChatMapper {

    // ── ChatMessage ───────────────────────────────────────────────

    public ChatMessageResponse toResponse(ChatMessage message) {
        if (message == null) return null;

        return new ChatMessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getAgentType(),
                message.getIsPinned(),
                message.getMessageIndex(),
                message.getCreatedAt()
        );
    }

    public List<ChatMessageResponse> toResponseList(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) return Collections.emptyList();
        return messages.stream()
                .map(this::toResponse)
                .toList();
    }

    // ── ChatSession ───────────────────────────────────────────────

    public ChatSessionResponse toResponse(ChatSession session) {
        if (session == null) return null;

        return new ChatSessionResponse(
                session.getId(),
                session.getSessionId(),
                session.getActiveAgentType(),
                session.getStatus(),
                session.getSummary() != null && !session.getSummary().isBlank(),
                toResponseList(session.getMessages()),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }

    // ── ChatSession без повідомлень (для списків / заголовків) ────
    // щоб не тягнути всі messages коли вони не потрібні

    public ChatSessionResponse toResponseLight(ChatSession session) {
        if (session == null) return null;

        return new ChatSessionResponse(
                session.getId(),
                session.getSessionId(),
                session.getActiveAgentType(),
                session.getStatus(),
                session.getSummary() != null && !session.getSummary().isBlank(),
                Collections.emptyList(), // без повідомлень
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }
}