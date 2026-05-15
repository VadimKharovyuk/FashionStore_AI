package com.example.fashionstore_ai.tools;

import com.example.fashionstore_ai.enums.AgentType;
import com.example.fashionstore_ai.model.AgentRegistry;
import com.example.fashionstore_ai.repository.AgentRegistryRepository;
import com.example.fashionstore_ai.tools.agent.RecommendationAgent;
import com.example.fashionstore_ai.tools.agent.ShoppingAssistant;
import com.example.fashionstore_ai.tools.agent.SizingAgent;
import com.example.fashionstore_ai.tools.agent.SupportAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "app.orchestrator.strategy", havingValue = "embedding")
@RequiredArgsConstructor
@Slf4j
public class EmbeddingOrchestratorAgent implements OrchestratorAgent {

    private final ShoppingAssistant shoppingAssistant;
    private final SizingAgent sizingAgent;
    private final SupportAgent supportAgent;
    private final RecommendationAgent recommendationAgent;
    private final AgentRegistryRepository agentRegistryRepository;
    private final EmbeddingModel embeddingModel;


    @Override
    public AgentType route(String userMessage) {
        float[] queryEmbedding = embeddingModel.embed(userMessage);

        String vectorStr = Arrays.stream(toDoubleArray(queryEmbedding))
                .mapToObj(Double::toString)
                .collect(Collectors.joining(",", "[", "]"));

        List<AgentRegistry> candidates = agentRegistryRepository
                .findTopByEmbeddingSimilarity(vectorStr, 1);

        if (candidates.isEmpty()) {
            log.warn("[Embedding] AgentRegistry порожній — fallback до SHOPPING_ASSISTANT");
            return AgentType.SHOPPING_ASSISTANT;
        }

        AgentType result = candidates.get(0).getAgentType();
        log.info("[Embedding] route → {} | '{}'", result, userMessage);
        return result;
    }


    @Override
    public Flux<String> chatStream(String sessionId, String userMessage, List<Message> history) {
        AgentType agent = route(userMessage);
        log.info("[Embedding] → {} | sessionId={}", agent, sessionId);
        return switch (agent) {
            case SIZING_AGENT         -> sizingAgent.chatStream(sessionId, userMessage, history);
            case SUPPORT_AGENT        -> supportAgent.chatStream(sessionId, userMessage, history);
            case RECOMMENDATION_AGENT -> recommendationAgent.chatStream(sessionId, userMessage, history);
            default                   -> shoppingAssistant.chatStream(sessionId, userMessage, history);
        };
    }

    @Override
    public Flux<String> chatStreamByType(AgentType agentType, String sessionId,
                                         String userMessage, List<Message> history) {
        log.info("[Hybrid] chatStreamByType → {} | sessionId={}", agentType, sessionId);
        return switch (agentType) {
            case SIZING_AGENT         -> sizingAgent.chatStream(sessionId, userMessage, history);
            case SUPPORT_AGENT        -> supportAgent.chatStream(sessionId, userMessage, history);
            case RECOMMENDATION_AGENT -> recommendationAgent.chatStream(sessionId, userMessage, history);
            default                   -> shoppingAssistant.chatStream(sessionId, userMessage, history);
        };
    }

    @Override
    public String chat(String sessionId, String userMessage, List<Message> history) {
        AgentType agent = route(userMessage);
        log.info("[Embedding] → {} | sessionId={}", agent, sessionId);
        return switch (agent) {
            case SIZING_AGENT         -> sizingAgent.chat(sessionId, userMessage, history);
            case SUPPORT_AGENT        -> supportAgent.chat(sessionId, userMessage, history);
            case RECOMMENDATION_AGENT -> recommendationAgent.chat(sessionId, userMessage, history);
            default                   -> shoppingAssistant.chat(sessionId, userMessage, history);
        };
    }

    private double[] toDoubleArray(float[] floats) {
        double[] doubles = new double[floats.length];
        for (int i = 0; i < floats.length; i++) doubles[i] = floats[i];
        return doubles;
    }
}
