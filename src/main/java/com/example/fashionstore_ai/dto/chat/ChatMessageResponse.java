package com.example.fashionstore_ai.dto.chat;
import com.example.fashionstore_ai.enums.AgentType;

import java.time.LocalDateTime;

public record ChatMessageResponse(

        Long id,
        String role,           // "user" / "assistant"
        String content,
        AgentType agentType,   // який агент відповів (null для user)
        Boolean isPinned,
        Integer messageIndex,
        LocalDateTime createdAt
) {}

