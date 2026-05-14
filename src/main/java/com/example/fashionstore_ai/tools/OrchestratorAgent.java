package com.example.fashionstore_ai.tools;

import com.example.fashionstore_ai.enums.AgentType;
import com.example.fashionstore_ai.tools.agent.ShoppingAssistant;
import com.example.fashionstore_ai.tools.agent.SizingAgent;
import com.example.fashionstore_ai.tools.agent.SupportAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrchestratorAgent {

    private final ShoppingAssistant shoppingAssistant;
    private final SizingAgent sizingAgent;
    private final SupportAgent supportAgent;

    // ── Ключові слова для маршрутизації ───────────────────────────

    private static final Set<String> SIZING_KEYWORDS = Set.of(
            "розмір", "розміри", "size", "груди", "талія", "стегна", "зріст",
            "параметри", "мірки", "підібрати розмір", "який розмір",
            "маломірить", "більшомірить", "підходить", "виміри",
            "chest", "waist", "hips", "height", "см", "розмірна сітка"
    );

    private static final Set<String> SUPPORT_KEYWORDS = Set.of(
            "замовлення", "доставка", "повернення", "трекінг", "статус",
            "скасувати", "обмін", "order", "tracking", "відправлено",
            "отримав", "не прийшло", "де посилка", "змінити адресу"
    );

    private static final Set<String> SHOPPING_KEYWORDS = Set.of(
            "шукаю", "знайди", "покажи", "є у вас", "хочу купити",
            "додай в кошик", "кошик", "ціна", "знижка", "новинки",
            "бестселер", "колекція", "сукня", "джинси", "куртка",
            "светр", "пальто", "топ", "штани", "блузка"
    );

    // ── Маршрутизація ─────────────────────────────────────────────

    public AgentType route(String userMessage) {
        String lower = userMessage.toLowerCase();

        int sizingScore   = scoreKeywords(lower, SIZING_KEYWORDS);
        int supportScore  = scoreKeywords(lower, SUPPORT_KEYWORDS);
        int shoppingScore = scoreKeywords(lower, SHOPPING_KEYWORDS);

        log.info("OrchestratorAgent.route: sizing={} support={} shopping={} message='{}'",
                sizingScore, supportScore, shoppingScore, userMessage);

        // вибираємо агента з найвищим score
        // при рівності — ShoppingAssistant як дефолт
        if (sizingScore > shoppingScore && sizingScore > supportScore) {
            return AgentType.SIZING_AGENT;
        }
        if (supportScore > shoppingScore && supportScore > sizingScore) {
            return AgentType.SUPPORT_AGENT;
        }
        return AgentType.SHOPPING_ASSISTANT;
    }

    // ── Делегування ───────────────────────────────────────────────

    public Flux<String> chatStream(String sessionId,
                                   String userMessage,
                                   List<Message> history) {
        AgentType agentType = route(userMessage);
        log.info("OrchestratorAgent: делегуємо до {} для sessionId={}",
                agentType, sessionId);

        return switch (agentType) {
            case SIZING_AGENT  -> sizingAgent.chatStream(sessionId, userMessage, history);
            case SUPPORT_AGENT -> supportAgent.chatStream(sessionId, userMessage, history);
            default            -> shoppingAssistant.chatStream(sessionId, userMessage, history);
        };
    }

    public String chat(String sessionId,
                       String userMessage,
                       List<Message> history) {
        AgentType agentType = route(userMessage);
        log.info("OrchestratorAgent: делегуємо до {} для sessionId={}",
                agentType, sessionId);

        return switch (agentType) {
            case SIZING_AGENT  -> sizingAgent.chat(sessionId, userMessage, history);
            case SUPPORT_AGENT -> supportAgent.chat(sessionId, userMessage, history);
            default            -> shoppingAssistant.chat(sessionId, userMessage, history);
        };
    }

    // ── Helper ────────────────────────────────────────────────────

    private int scoreKeywords(String text, Set<String> keywords) {
        return (int) keywords.stream()
                .filter(text::contains)
                .count();
    }
}