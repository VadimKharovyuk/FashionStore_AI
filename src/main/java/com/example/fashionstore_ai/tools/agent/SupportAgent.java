package com.example.fashionstore_ai.tools.agent;

import com.example.fashionstore_ai.tools.OrderTool;
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
public class SupportAgent {

    private final ChatClient chatClient;
    private final OrderTool orderTool;

    private static final String SYSTEM_PROMPT = """
            Ти — агент підтримки інтернет-магазину FashionStore.
            Твоя роль: допомагати з питаннями по замовленнях, доставці і поверненнях.

            Що ти вмієш:
            - Показувати статус і деталі замовлення
            - Відстежувати доставку (трекінг)
            - Скасовувати замовлення (тільки PENDING і CONFIRMED)
            - Змінювати адресу доставки (тільки PENDING і CONFIRMED)
            - Оформляти повернення товарів (тільки DELIVERED)
            - Пояснювати політику повернень

            Чого ти НЕ робиш:
            - Не шукаєш товари (для цього є ShoppingAssistant)
            - Не підбираєш розміри (для цього є SizingAgent)

            Правила роботи:
            1. ЗАВЖДИ запитуй підтвердження перед скасуванням або поверненням
            2. Перш ніж виконати дію — показуй деталі замовлення через getOrderByNumber
            3. Якщо користувач не знає номер замовлення — запропонуй getMyOrders
            4. Для повернення — уточни причину і конкретний товар
            5. Будь емпатичним і терплячим — людина може бути засмучена
            6. Відповідай українською мовою
            """;

    public Flux<String> chatStream(String sessionId, String userMessage, List<Message> history) {
        log.info("SupportAgent.chatStream: sessionId={}", sessionId);

        String systemWithSession = SYSTEM_PROMPT +
                "\n\nПОТОЧНА СЕСІЯ КОРИСТУВАЧА: " + sessionId +
                "\nВикористовуй ТІЛЬКИ цей sessionId у всіх tool викликах.";

        return chatClient.prompt()
                .system(systemWithSession)
                .messages(history)
                .user(userMessage)
                .tools(orderTool)
                .stream()
                .content()
                .doOnError(e -> log.error("SupportAgent stream error: sessionId={}", sessionId, e));
    }

    public String chat(String sessionId, String userMessage, List<Message> history) {
        log.info("SupportAgent.chat: sessionId={}", sessionId);

        String systemWithSession = SYSTEM_PROMPT +
                "\n\nПОТОЧНА СЕСІЯ КОРИСТУВАЧА: " + sessionId +
                "\nВикористовуй ТІЛЬКИ цей sessionId у всіх tool викликах.";

        try {
            return chatClient.prompt()
                    .system(systemWithSession)
                    .messages(history)
                    .user(userMessage)
                    .tools(orderTool)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("SupportAgent error: sessionId={}", sessionId, e);
            return "Вибач, виникла помилка. Спробуй ще раз або зв'яжись з підтримкою.";
        }
    }
}
