package com.example.fashionstore_ai.tools.agent;

import com.example.fashionstore_ai.tools.CartTool;
import com.example.fashionstore_ai.tools.ProductSearchTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@Slf4j
public class ShoppingAssistant {

    private final ChatClient chatClient;
    private final ProductSearchTool productSearchTool;
    private final CartTool cartTool;

    public ShoppingAssistant(@Qualifier("smartChatClient") ChatClient chatClient,
                             ProductSearchTool productSearchTool,
                             CartTool cartTool) {
        this.chatClient = chatClient;
        this.productSearchTool = productSearchTool;
        this.cartTool = cartTool;
    }

    private static final String SYSTEM_PROMPT = """
            Ти — AI-помічник інтернет-магазину одягу FashionStore.
            Твоя роль: допомагати знаходити одяг, підбирати речі та керувати кошиком.

            Що ти вмієш:
            - Шукати товари за категорією, кольором, матеріалом, ціною, сезоном
            - Показувати деталі товарів (склад тканини, догляд, розміри)
            - Перевіряти наявність розмірів на складі
            - Додавати і видаляти товари з кошика
            - Показувати вміст кошика і загальну суму

            Чого ти НЕ робиш:
            - Не підбираєш розмір по параметрах тіла (для цього є SizingAgent)
            - Не відповідаєш на питання про замовлення і доставку (для цього є SupportAgent)
            - Не даєш персональних рекомендацій (для цього є RecommendationAgent)

            Правила роботи:
            1. ЗАВЖДИ викликай searchProducts перед будь-якою відповіддю про товари
            2. НІКОЛИ не відповідай про наявність товарів без виклику searchProducts
            3. Перед додаванням в кошик ЗАВЖДИ перевіряй наявність через checkStock
            4. Якщо tool повернув found=false — слідуй інструкції з tool response
            5. При показі товарів ЗАВЖДИ використовуй формат: [Назва](/products/ID) | Бренд | $Ціна
            6. Якщо запитання не стосується пошуку товарів або кошика —
               поясни що передаєш питання відповідному спеціалісту

            МОВА ВІДПОВІДІ:
            - Відповідай ТІЄЮ САМОЮ мовою якою написав користувач
            - Користувач пише російською → відповідай російською
            - Користувач пише українською → відповідай українською
            - Користувач пише англійською → відповідай англійською

            ЗАБОРОНЕНО:
            - Вигадувати товари яких немає у відповіді tool
            - Додавати ID товарів яких не було у результатах пошуку
            - Пропонувати "схожі варіанти" або "альтернативи" зі своєї голови
            - Якщо tool повернув порожній результат — скажи що не знайдено
              і запитай чи змінити фільтр (колір, матеріал, категорію). Більше нічого не додавай.
            """;

    public Flux<String> chatStream(String sessionId, String userMessage, List<Message> history) {
//        log.info("ShoppingAssistant.chatStream: sessionId={}", sessionId);

        String systemWithSession = SYSTEM_PROMPT +
                "\n\nПОТОЧНА СЕСІЯ КОРИСТУВАЧА: " + sessionId +
                "\nВикористовуй ТІЛЬКИ цей sessionId у всіх tool викликах. Не вигадуй інший.";

        StringBuilder tokenBuffer = new StringBuilder();

        return chatClient.prompt()
                .system(systemWithSession)
                .messages(history)
                .user(userMessage)
                .tools(productSearchTool, cartTool)
                .stream()
                .content()
                .flatMap(token -> {
                    tokenBuffer.append(token);
                    String current = tokenBuffer.toString();
                    String normalized = current
                            .replaceAll("([а-яА-ЯіІїЇєЄa-zA-Z0-9])([А-ЯІЇЄA-Z*#|])", "$1 $2")
                            .replaceAll("([*|])([а-яА-ЯіІa-zA-Z0-9])", "$1 $2")
                            .replaceAll("([а-яА-ЯіІa-zA-Z0-9])([*|])", "$1 $2");
                    tokenBuffer.setLength(0);
                    return Flux.just(normalized);
                })
                .doOnError(e -> log.error("ShoppingAssistant stream error: sessionId={}", sessionId, e));
    }

    public String chat(String sessionId, String userMessage, List<Message> history) {
        log.info("ShoppingAssistant.chat: sessionId={} message='{}'", sessionId, userMessage);

        String systemWithSession = SYSTEM_PROMPT +
                "\n\nПОТОЧНА СЕСІЯ КОРИСТУВАЧА: " + sessionId +
                "\nВикористовуй ТІЛЬКИ цей sessionId у всіх tool викликах.";

        try {
            return chatClient.prompt()
                    .system(systemWithSession)
                    .messages(history)
                    .user(userMessage)
                    .tools(productSearchTool, cartTool)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("ShoppingAssistant error: sessionId={}", sessionId, e);
            return "Вибач, виникла помилка при обробці запиту. Спробуй ще раз.";
        }
    }

    public String chat(String sessionId, String userMessage) {
        return chat(sessionId, userMessage, List.of());
    }
}