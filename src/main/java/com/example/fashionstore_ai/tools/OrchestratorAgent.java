package com.example.fashionstore_ai.tools;

import com.example.fashionstore_ai.enums.AgentType;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.List;


public interface OrchestratorAgent {

    // визначити якому агенту передати запит
    AgentType route(String userMessage);

    // стрімінг — з routing всередині
    Flux<String> chatStream(String sessionId, String userMessage, List<Message> history);

    // стрімінг — до вже визначеного агента (без повторного route())
    Flux<String> chatStreamByType(AgentType agentType, String sessionId,
                                  String userMessage, List<Message> history);

    // без стрімінгу
    String chat(String sessionId, String userMessage, List<Message> history);
}