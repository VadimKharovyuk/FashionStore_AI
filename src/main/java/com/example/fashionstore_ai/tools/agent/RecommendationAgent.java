package com.example.fashionstore_ai.tools.agent;
import com.example.fashionstore_ai.tools.RecommendationTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationAgent {

    private final ChatClient chatClient;
    private final RecommendationTool recommendationTool;

    private static final String SYSTEM_PROMPT = """
            Ти — персональний стиліст і агент рекомендацій магазину FashionStore.
            Твоя роль: давати персональні рекомендації товарів на основі смаків користувача.

            Що ти вмієш:
            - Давати персональні рекомендації на основі переглянутих товарів
            - Показувати схожі товари як альтернативи
            - Підбирати доповнюючі товари (з чим носити)
            - Показувати хіти продажів і новинки
            - Відновлювати переглянуті товари

            Чого ти НЕ робиш:
            - Не додаєш товари в кошик (для цього є ShoppingAssistant)
            - Не підбираєш розміри (для цього є SizingAgent)
            - Не відповідаєш на питання про замовлення (для цього є SupportAgent)

            Правила роботи:
            1. ЗАВЖДИ починай з getPersonalizedRecommendations — це твій головний інструмент
            2. Додавай контекст до рекомендацій — пояснюй ЧОМУ рекомендуєш саме це
            3. Після рекомендацій пропонуй доповнюючі товари
            4. Відповідай як справжній стиліст — з захопленням і експертизою
            5. Відповідай українською мовою
            """;

    public Flux<String> chatStream(String sessionId, String userMessage, List<Message> history) {
        log.info("RecommendationAgent.chatStream: sessionId={}", sessionId);

        String systemWithSession = SYSTEM_PROMPT +
                "\n\nПОТОЧНА СЕСІЯ КОРИСТУВАЧА: " + sessionId +
                "\nВикористовуй ТІЛЬКИ цей sessionId у всіх tool викликах.";

        return chatClient.prompt()
                .system(systemWithSession)
                .messages(history)
                .user(userMessage)
                .tools(recommendationTool)
                .stream()
                .content()
                .doOnError(e -> log.error("RecommendationAgent error: sessionId={}", sessionId, e));
    }

    public String chat(String sessionId, String userMessage, List<Message> history) {
        log.info("RecommendationAgent.chat: sessionId={}", sessionId);

        String systemWithSession = SYSTEM_PROMPT +
                "\n\nПОТОЧНА СЕСІЯ КОРИСТУВАЧА: " + sessionId +
                "\nВикористовуй ТІЛЬКИ цей sessionId у всіх tool викликах.";

        try {
            return chatClient.prompt()
                    .system(systemWithSession)
                    .messages(history)
                    .user(userMessage)
                    .tools(recommendationTool)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("RecommendationAgent error: sessionId={}", sessionId, e);
            return "Вибач, виникла помилка. Спробуй ще раз.";
        }
    }
}
