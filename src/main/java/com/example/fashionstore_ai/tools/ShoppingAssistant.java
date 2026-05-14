package com.example.fashionstore_ai.tools;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShoppingAssistant {

    private final ChatClient chatClient;
    private final ProductSearchTool productSearchTool;
    private final CartTool cartTool;

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
            1. Перед додаванням в кошик ЗАВЖДИ перевіряй наявність через checkStock
            2. Якщо товар не знайдено — пропонуй змінити фільтри
            3. Відповідай українською мовою, дружньо і лаконічно
            4. При показі товарів завжди вказуй ID — він потрібен для додавання в кошик
            5. Якщо запитання не стосується пошуку товарів або кошика —
               поясни що передаєш питання відповідному спеціалісту
            """;

    public String chat(String sessionId, String userMessage, List<Message> history) {
        log.info("ShoppingAssistant.chat: sessionId={} message='{}'",
                sessionId, userMessage);

        try {
            String response = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .messages(history)
                    .user(userMessage)
                    .tools(productSearchTool, cartTool)
                    .call()
                    .content();

            log.debug("ShoppingAssistant response: '{}'", response);
            return response;

        } catch (Exception e) {
            log.error("ShoppingAssistant error: sessionId={}", sessionId, e);
            return "Вибач, виникла помилка при обробці запиту. Спробуй ще раз.";
        }
    }

    // зручний метод без history — для першого повідомлення
    public String chat(String sessionId, String userMessage) {
        return chat(sessionId, userMessage, List.of());
    }
}
