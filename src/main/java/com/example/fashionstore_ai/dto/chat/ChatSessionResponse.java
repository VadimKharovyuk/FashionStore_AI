package com.example.fashionstore_ai.dto.chat;
import com.example.fashionstore_ai.enums.AgentType;

import java.time.LocalDateTime;
import java.util.List;

public record ChatSessionResponse(

        Long id,
        String sessionId,
        AgentType activeAgentType,
        String status,

        // summary є — значить сесія довга, є стиснутий контекст
        boolean hasSummary,

        List<ChatMessageResponse> messages,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
