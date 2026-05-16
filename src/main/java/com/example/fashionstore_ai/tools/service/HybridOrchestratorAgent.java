package com.example.fashionstore_ai.tools.service;

import com.example.fashionstore_ai.enums.AgentType;
import com.example.fashionstore_ai.model.AgentRegistry;
import com.example.fashionstore_ai.repository.AgentRegistryRepository;
import com.example.fashionstore_ai.tools.OrchestratorAgent;
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
import java.util.Set;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "app.orchestrator.strategy", havingValue = "hybrid")
@RequiredArgsConstructor
@Slf4j
public class HybridOrchestratorAgent implements OrchestratorAgent {

    private final ShoppingAssistant shoppingAssistant;
    private final SizingAgent sizingAgent;
    private final SupportAgent supportAgent;
    private final RecommendationAgent recommendationAgent;
    private final AgentRegistryRepository agentRegistryRepository;
    private final EmbeddingModel embeddingModel;

    // якщо keyword score >= порогу — довіряємо keyword, не викликаємо embedding
    private static final int KEYWORD_CONFIDENCE_THRESHOLD = 2;

    private static final Set<String> SIZING_KEYWORDS = Set.of(
            "розмір", "розміри", "size", "груди", "талія", "стегна", "зріст",
            "параметри", "мірки", "підібрати розмір", "який розмір",
            "маломірить", "більшомірить", "виміри", "розмірна сітка",
            "chest", "waist", "hips", "height", "см", "cm",
            "підходить розмір", "мій розмір", "розмір підійде",
            "s m l xl", "xs s m"
    );

    private static final Set<String> SUPPORT_KEYWORDS = Set.of(
            "замовлення", "доставка", "повернення", "трекінг", "статус",
            "скасувати", "обмін", "order", "tracking", "відправлено",
            "отримав", "не прийшло", "де посилка", "змінити адресу",
            "ord-", "номер замовлення", "посилка", "кур'єр",
            "не отримав", "затримка", "повернути гроші", "рефанд"
    );

    private static final Set<String> SHOPPING_KEYWORDS = Set.of(
            "шукаю", "знайди", "є у вас", "хочу купити",
            "додай в кошик", "кошик", "ціна", "знижка", "до ", "від ",
            "сукня", "джинси", "куртка", "светр", "пальто",
            "топ", "штани", "блузка", "сорочка", "худі", "піджак",
            "спідниця", "легінси", "шорти", "плащ", "футболка",
            "чорний", "білий", "синій", "червоний", "бежевий",
            "бавовна", "шовк", "вовна", "шкіра",
            "zara", "mango", "h&m", "reserved", "massimo dutti",
            "є в наявності", "розпродаж", "акція", "новий"
    );

    private static final Set<String> RECOMMENDATION_KEYWORDS = Set.of(
            "порадь", "порекомендуй", "що порадиш", "рекомендації",
            "підбери", "підбери мені", "підбери образ",
            "схоже", "альтернатива", "з чим носити", "що підійде",
            "хіти", "популярне", "нова колекція", "новинки",
            "що нового", "переглянуті", "що я дивився", "персональне",
            "що одягнути", "на вечірку", "на побачення", "для офісу",
            "на свято", "на весілля", "casual look", "образ",
            "стиліст", "порада стиліста", "модно", "трендово",
            // русские варианты
            "посоветуй", "порекомендуй", "подбери", "что надеть",
            "что посоветуешь", "рекомендации", "хиты", "новинки",
            "похожее", "альтернатива", "с чем носить"
    );

    @Override
    public AgentType route(String userMessage) {
        String lower = userMessage.toLowerCase();

        // ── Крок 1: keyword scoring ───────────────────────────────
        int sizingScore         = score(lower, SIZING_KEYWORDS);
        int supportScore        = score(lower, SUPPORT_KEYWORDS);
        int shoppingScore       = score(lower, SHOPPING_KEYWORDS);
        int recommendScore      = score(lower, RECOMMENDATION_KEYWORDS);

        int maxScore = Math.max(Math.max(sizingScore, supportScore),
                Math.max(shoppingScore, recommendScore));

        // ── Крок 2: якщо є впевнений keyword match — повертаємо одразу
        if (maxScore >= KEYWORD_CONFIDENCE_THRESHOLD) {
            AgentType keywordResult = resolveByScore(
                    sizingScore, supportScore, shoppingScore, recommendScore);
            log.info("[Hybrid] keyword match (score={}) → {} | '{}'",
                    maxScore, keywordResult, userMessage);
            return keywordResult;
        }

        // ── Крок 3: низький score — використовуємо embedding ─────
        log.info("[Hybrid] low keyword score ({}) → використовуємо embedding | '{}'",
                maxScore, userMessage);

        try {
            float[] queryEmbedding = embeddingModel.embed(userMessage);
            String vectorStr = Arrays.stream(toDoubleArray(queryEmbedding))
                    .mapToObj(Double::toString)
                    .collect(Collectors.joining(",", "[", "]"));

            List<AgentRegistry> candidates = agentRegistryRepository
                    .findTopByEmbeddingSimilarity(vectorStr, 1);

            if (!candidates.isEmpty()) {
                AgentType embeddingResult = candidates.get(0).getAgentType();
                log.info("[Hybrid] embedding → {} | '{}'", embeddingResult, userMessage);
                return embeddingResult;
            }
        } catch (Exception e) {
            log.warn("[Hybrid] embedding failed, fallback to keyword: {}", e.getMessage());
        }

        // ── Крок 4: fallback — keyword навіть при низькому score ─
        if (maxScore > 0) {
            AgentType fallback = resolveByScore(
                    sizingScore, supportScore, shoppingScore, recommendScore);
            log.info("[Hybrid] fallback keyword → {} | '{}'", fallback, userMessage);
            return fallback;
        }

        return AgentType.SHOPPING_ASSISTANT;
    }

    @Override
    public Flux<String> chatStream(String sessionId, String userMessage, List<Message> history) {
        AgentType agent = route(userMessage);
        log.info("[Hybrid] → {} | sessionId={}", agent, sessionId);
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
        log.info("[Hybrid] → {} | sessionId={}", agent, sessionId);
        return switch (agent) {
            case SIZING_AGENT         -> sizingAgent.chat(sessionId, userMessage, history);
            case SUPPORT_AGENT        -> supportAgent.chat(sessionId, userMessage, history);
            case RECOMMENDATION_AGENT -> recommendationAgent.chat(sessionId, userMessage, history);
            default                   -> shoppingAssistant.chat(sessionId, userMessage, history);
        };
    }

    // ── Helpers ───────────────────────────────────────────────────

    private AgentType resolveByScore(int sizing, int support, int shopping, int recommend) {
        if (recommend > sizing && recommend > support && recommend > shopping)
            return AgentType.RECOMMENDATION_AGENT;
        if (sizing > support && sizing > shopping)
            return AgentType.SIZING_AGENT;
        if (support > shopping)
            return AgentType.SUPPORT_AGENT;
        return AgentType.SHOPPING_ASSISTANT;
    }

    private int score(String text, Set<String> keywords) {
        return (int) keywords.stream().filter(text::contains).count();
    }

    private double[] toDoubleArray(float[] floats) {
        double[] doubles = new double[floats.length];
        for (int i = 0; i < floats.length; i++) doubles[i] = floats[i];
        return doubles;
    }
}