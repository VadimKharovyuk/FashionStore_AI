package com.example.fashionstore_ai.tools.agent;

import com.example.fashionstore_ai.tools.RecommendationTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@Slf4j
public class RecommendationAgent {

    private final ChatClient chatClient;
    private final RecommendationTool recommendationTool;

    public RecommendationAgent(@Qualifier("smartChatClient") ChatClient chatClient,
                               RecommendationTool recommendationTool) {
        this.chatClient = chatClient;
        this.recommendationTool = recommendationTool;
    }

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
            2. Кожен товар ОБОВ'ЯЗКОВО виводь у форматі: [Назва товару](/products/ID)
               Наприклад: [Сукня вечірня](/products/1) | Zara | $89.99
            3. Описуй товар ТІЛЬКИ на основі даних які повернув tool — не вигадуй характеристики
            4. Додавай контекст до рекомендацій — пояснюй ЧОМУ рекомендуєш саме це
            5. Якщо хочеш запропонувати доповнюючі товари — викликай tool getComplementaryProducts,
           НЕ вигадуй їх самостійно
            6. Відповідай як справжній стиліст — коротко і по суті, без зайвих слів
            7. НЕ вигадуй ID товарів — використовуй тільки ті що повернув tool
            8. Якщо товар не підходить під запит — не включай його у відповідь

            МОВА ВІДПОВІДІ:
            - Відповідай ТІЄЮ САМОЮ мовою якою написав користувач
            - Користувач пише російською → відповідай російською
            - Користувач пише українською → відповідай українською
            - Користувач пише англійською → відповідай англійською
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