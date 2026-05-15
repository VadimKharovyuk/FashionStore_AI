package com.example.fashionstore_ai.tools;

import com.example.fashionstore_ai.enums.AgentType;
import com.example.fashionstore_ai.tools.agent.RecommendationAgent;
import com.example.fashionstore_ai.tools.agent.ShoppingAssistant;
import com.example.fashionstore_ai.tools.agent.SizingAgent;
import com.example.fashionstore_ai.tools.agent.SupportAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "app.orchestrator.strategy", havingValue = "keyword", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class KeywordOrchestratorAgent implements OrchestratorAgent {

    private final ShoppingAssistant shoppingAssistant;
    private final SizingAgent sizingAgent;
    private final SupportAgent supportAgent;
    private final RecommendationAgent recommendationAgent;

    private static final Set<String> SIZING_KEYWORDS = Set.of(
            "розмір", "розміри", "size", "груди", "талія", "стегна", "зріст",
            "параметри", "мірки", "підібрати розмір", "який розмір",
            "маломірить", "більшомірить", "виміри",
            "chest", "waist", "hips", "height", "см", "розмірна сітка"
    );

    private static final Set<String> SUPPORT_KEYWORDS = Set.of(
            "замовлення", "доставка", "повернення", "трекінг", "статус",
            "скасувати", "обмін", "order", "tracking", "відправлено",
            "отримав", "не прийшло", "де посилка", "змінити адресу"
    );

    private static final Set<String> SHOPPING_KEYWORDS = Set.of(
            "шукаю", "знайди", "покажи", "є у вас", "хочу купити",
            "додай в кошик", "кошик", "ціна", "знижка",
            "бестселер", "колекція", "сукня", "джинси", "куртка",
            "светр", "пальто", "топ", "штани", "блузка"
    );

    private static final Set<String> RECOMMENDATION_KEYWORDS = Set.of(
            "порадь", "порекомендуй", "що порадиш", "рекомендації", "підбери",
            "схоже", "альтернатива", "з чим носити",
            "хіти", "популярне", "нова колекція",
            "що нового", "переглянуті", "що я дивився", "персональне"
    );

    @Override
    public AgentType route(String userMessage) {
        String lower = userMessage.toLowerCase();

        int sizingScore         = score(lower, SIZING_KEYWORDS);
        int supportScore        = score(lower, SUPPORT_KEYWORDS);
        int shoppingScore       = score(lower, SHOPPING_KEYWORDS);
        int recommendationScore = score(lower, RECOMMENDATION_KEYWORDS);

        log.info("[Keyword] route: sizing={} support={} shopping={} recommendation={} | '{}'",
                sizingScore, supportScore, shoppingScore, recommendationScore, userMessage);

        int max = Math.max(Math.max(sizingScore, supportScore),
                Math.max(shoppingScore, recommendationScore));

        if (max == 0)                        return AgentType.SHOPPING_ASSISTANT;
        if (recommendationScore == max)      return AgentType.RECOMMENDATION_AGENT;
        if (sizingScore         == max)      return AgentType.SIZING_AGENT;
        if (supportScore        == max)      return AgentType.SUPPORT_AGENT;
        return AgentType.SHOPPING_ASSISTANT;
    }

    @Override
    public Flux<String> chatStream(String sessionId, String userMessage, List<Message> history) {
        AgentType agent = route(userMessage);
        log.info("[Keyword] → {} | sessionId={}", agent, sessionId);
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
        log.info("[Keyword] → {} | sessionId={}", agent, sessionId);
        return switch (agent) {
            case SIZING_AGENT         -> sizingAgent.chat(sessionId, userMessage, history);
            case SUPPORT_AGENT        -> supportAgent.chat(sessionId, userMessage, history);
            case RECOMMENDATION_AGENT -> recommendationAgent.chat(sessionId, userMessage, history);
            default                   -> shoppingAssistant.chat(sessionId, userMessage, history);
        };
    }

    private int score(String text, Set<String> keywords) {
        return (int) keywords.stream().filter(text::contains).count();
    }
}