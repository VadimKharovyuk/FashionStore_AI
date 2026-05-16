package com.example.fashionstore_ai.tools.agent;

import com.example.fashionstore_ai.config.BaseTool;
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
public class SizingAgent {

    private final ChatClient chatClient;
    private final SizingTool sizingTool;

    private static final String SYSTEM_PROMPT = """
            Ти — AI-помічник з підбору розмірів одягу в магазині FashionStore.
            Твоя роль: допомагати користувачам знайти правильний розмір на основі параметрів тіла.

            Що ти вмієш:
            - Зберігати параметри тіла (груди, талія, стегна, зріст)
            - Показувати розмірну сітку конкретного бренду
            - Підбирати розмір і пояснювати чому саме він підходить
            - Враховувати особливості бренду (маломірить, більшомірить)
            - Давати альтернативу якщо користувач на межі розмірів

            Чого ти НЕ робиш:
            - Не шукаєш товари і не додаєш в кошик (для цього є ShoppingAssistant)
            - Не відповідаєш на питання про замовлення (для цього є SupportAgent)

            Правила роботи:
            1. ЗАВЖДИ спочатку перевіряй чи є збережені параметри через getUserMeasurements
            2. Якщо параметрів немає — ввічливо попроси вказати груди, талію і стегна в см
            3. Якщо користувач назвав параметри — ОДРАЗУ зберігай через saveUserMeasurements
            4. Після підбору розміру — запропонуй показати товари цього бренду потрібного розміру
            5. Відповідай українською, чітко і з поясненнями
            6. При підборі завжди вказуй конкретні цифри з розмірної сітки
            """;

    public Flux<String> chatStream(String sessionId, String userMessage, List<Message> history) {
        log.info("SizingAgent.chatStream: sessionId={}", sessionId);

        BaseTool.setCurrentSessionId(sessionId);

        String systemWithSession = SYSTEM_PROMPT +
                "\n\nПОТОЧНА СЕСІЯ КОРИСТУВАЧА: " + sessionId +
                "\nВикористовуй ТІЛЬКИ цей sessionId у всіх tool викликах: " + sessionId;

        String messageWithSession = "[sessionId=" + sessionId + "] " + userMessage;

        return chatClient.prompt()
                .system(systemWithSession)
                .messages(history)
                .user(messageWithSession)
                .tools(sizingTool)
                .stream()
                .content()
                .doOnError(e -> log.error("SizingAgent stream error: sessionId={}", sessionId, e))
                .doFinally(signal -> BaseTool.clearCurrentSessionId());
    }

    public String chat(String sessionId, String userMessage, List<Message> history) {
        log.info("SizingAgent.chat: sessionId={}", sessionId);

        BaseTool.setCurrentSessionId(sessionId); // ← добавить

        String systemWithSession = SYSTEM_PROMPT +
                "\n\nПОТОЧНА СЕСІЯ КОРИСТУВАЧА: " + sessionId +
                "\nВикористовуй ТІЛЬКИ цей sessionId у всіх tool викликах: " + sessionId;

        String messageWithSession = "[sessionId=" + sessionId + "] " + userMessage;

        try {
            return chatClient.prompt()
                    .system(systemWithSession)
                    .messages(history)
                    .user(messageWithSession)
                    .tools(sizingTool)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("SizingAgent error: sessionId={}", sessionId, e);
            return "Вибач, виникла помилка при підборі розміру. Спробуй ще раз.";
        } finally {
            BaseTool.clearCurrentSessionId(); // ← добавить
        }
    }
}