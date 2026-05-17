package com.example.fashionstore_ai.service;

import com.example.fashionstore_ai.model.ChatMessage;
import com.example.fashionstore_ai.model.ChatSession;
import com.example.fashionstore_ai.repository.ChatMessageRepository;
import com.example.fashionstore_ai.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
@RequiredArgsConstructor
@Slf4j
public class ContextService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final OllamaChatModel ollamaChatModel;
    private final ObjectMapper objectMapper;

    private static final int HISTORY_SIZE      = 20;
    private static final int PINNED_COUNT      = 3;
    private static final int SUMMARY_THRESHOLD = 30;
    private static final int KEEP_RECENT       = 15;

//    private static final int SUMMARY_THRESHOLD = 4;  // було 30
//    private static final int KEEP_RECENT       = 2;  // було 15

    // ── buildHistory ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Message> buildHistory(ChatSession session) {
        List<ChatMessage> allMessages = chatMessageRepository
                .findByChatSessionIdOrderByCreatedAtAsc(session.getId());

        if (allMessages.isEmpty()) return Collections.emptyList();

        List<Message> result = new ArrayList<>();

        if (session.getSummary() != null && !session.getSummary().isBlank()) {
            result.add(new SystemMessage(
                    "[Резюме попередньої розмови]: " + session.getSummary()));
        }

        // Додаємо критичні факти в контекст якщо є
        if (session.getCriticalFacts() != null && !session.getCriticalFacts().isEmpty()) {
            result.add(new SystemMessage(
                    "[Відомі факти про користувача]: " + session.getCriticalFacts()));
        }

        int pinnedCount = Math.min(PINNED_COUNT, allMessages.size());
        allMessages.subList(0, pinnedCount)
                .forEach(m -> result.add(toSpringAiMessage(m)));

        int skipTo = Math.max(pinnedCount, allMessages.size() - HISTORY_SIZE);
        if (skipTo < allMessages.size()) {
            allMessages.subList(skipTo, allMessages.size())
                    .forEach(m -> result.add(toSpringAiMessage(m)));
        }

        log.debug("ContextService.buildHistory: total={} pinned={} window={} result={}",
                allMessages.size(), pinnedCount,
                allMessages.size() - skipTo, result.size());

        return result;
    }

    // ── summarizeIfNeeded — БЕЗ @Transactional ───────────────────

    public void summarizeIfNeeded(ChatSession session) {
        if (!needsSummarization(session)) return;

        log.info("ContextService: запускаємо summarization для sessionId={} (messages={})",
                session.getSessionId(),
                chatMessageRepository.countByChatSessionId(session.getId()));

        // Крок 1: читаємо дані → транзакція відкрилась і одразу закрилась
        List<ChatMessage> toSummarize = getMessagesForSummarization(session);
        if (toSummarize.isEmpty()) return;

        long totalMessages = chatMessageRepository.countByChatSessionId(session.getId());
        int summarizeUpTo  = (int) (totalMessages - KEEP_RECENT);

        // Крок 2а: генеруємо summary БЕЗ відкритої транзакції
        String newSummary = generateSummary(toSummarize, session.getSummary());
        if (newSummary.isBlank()) return;

        // Крок 2б: витягуємо критичні факти — окремий LLM call, БД все ще вільна
        Map<String, Object> newFacts = extractCriticalFacts(toSummarize, session.getCriticalFacts());

        // Крок 3: зберігаємо разом → коротка нова транзакція
        saveSummary(session, newSummary, summarizeUpTo, newFacts);
    }

    @Transactional(readOnly = true)
    public boolean needsSummarization(ChatSession session) {
        return chatMessageRepository.countByChatSessionId(session.getId()) > SUMMARY_THRESHOLD;
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getMessagesForSummarization(ChatSession session) {
        long total         = chatMessageRepository.countByChatSessionId(session.getId());
        int  summarizeUpTo = (int) (total - KEEP_RECENT);
        return chatMessageRepository.findForSummarization(session.getId(), summarizeUpTo);
    }

    @Transactional
    public void saveSummary(ChatSession session, String summary,
                            int summarizedCount, Map<String, Object> facts) {
        session.setSummary(summary);
        session.setSummaryUpdatedAt(LocalDateTime.now());
        session.setSummarizedMessagesCount(summarizedCount);
        session.setCriticalFacts(facts);
        chatSessionRepository.save(session);
        log.info("ContextService: summary збережено ({} символів), facts={}",
                summary.length(), facts);
    }

    // ── saveUserMessage ───────────────────────────────────────────

    @Transactional
    public ChatMessage saveUserMessage(ChatSession session, String content, int messageIndex) {
        ChatMessage msg = ChatMessage.builder()
                .chatSession(session)
                .role("user")
                .content(content)
                .isPinned(messageIndex < PINNED_COUNT)
                .messageIndex(messageIndex)
                .build();
        return chatMessageRepository.save(msg);
    }

    // ── saveAssistantMessage ──────────────────────────────────────

    @Transactional
    public ChatMessage saveAssistantMessage(ChatSession session, String content,
                                            int messageIndex,
                                            com.example.fashionstore_ai.enums.AgentType agentType) {
        ChatMessage msg = ChatMessage.builder()
                .chatSession(session)
                .role("assistant")
                .content(content)
                .agentType(agentType)
                .isPinned(messageIndex < PINNED_COUNT)
                .messageIndex(messageIndex)
                .build();
        return chatMessageRepository.save(msg);
    }

    // ── Private helpers ───────────────────────────────────────────

    private Message toSpringAiMessage(ChatMessage msg) {
        return switch (msg.getRole()) {
            case "user"      -> new UserMessage(msg.getContent());
            case "assistant" -> new AssistantMessage(msg.getContent());
            default          -> new SystemMessage(msg.getContent());
        };
    }

    private String generateSummary(List<ChatMessage> messages, String existingSummary) {
        String historyText = messages.stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));

        String previousContext = (existingSummary != null && !existingSummary.isBlank())
                ? "Попереднє резюме: " + existingSummary + "\n\nНові повідомлення:\n"
                : "";

        String prompt = """
                Стисни цю частину розмови в 3-5 речень.
                Збережи: ключові факти, рішення, важливі деталі про користувача
                (що шукав, які розміри, що додав в кошик, параметри тіла якщо були).
                Відкинь: привітання, повтори, несуттєві деталі.

                %s%s
                """.formatted(previousContext, historyText);

        try {
            return ollamaChatModel.call(prompt).trim();
        } catch (Exception e) {
            log.error("ContextService: помилка при генерації summary", e);
            return existingSummary != null ? existingSummary : "";
        }
    }

    private Map<String, Object> extractCriticalFacts(List<ChatMessage> messages,
                                                     Map<String, Object> existingFacts) {
        String historyText = messages.stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));

        Map<String, Object> result = new HashMap<>();
        if (existingFacts != null) result.putAll(existingFacts);

        // Кожен факт — окремий простий запит
        extractSingleFact(historyText, "size",
                "Який розмір одягу згадав користувач? (XS/S/M/L/XL/XXL). Відповідь: тільки розмір або null")
                .ifPresent(v -> result.put("size", v));

        extractSingleFact(historyText, "budget",
                "Який максимальний бюджет згадав користувач? Відповідь: тільки число або null")
                .ifPresent(v -> result.put("budget", v));

        extractSingleFact(historyText, "cart_items",
                "Які id товарів додали в кошик? id є в посиланнях /products/ID. Відповідь: тільки числа через кому або null")
                .ifPresent(v -> result.put("cart_items", v));

        extractSingleFact(historyText, "in_stock_only",
                "Користувач хоче тільки товари в наявності? Відповідь: true або false")
                .ifPresent(v -> result.put("in_stock_only", v));

        log.info("extractCriticalFacts result: {}", result);
        return result;
    }

    private Optional<String> extractSingleFact(String historyText, String factName, String question) {
        String prompt = """
            Розмова:
            %s
            
            Питання: %s
            Якщо інформації немає — відповідь: null
            Відповідь одним словом або числом, без пояснень:
            """.formatted(historyText, question);
        try {
            String answer = ollamaChatModel.call(prompt).trim();
            if (answer.isBlank() || answer.equalsIgnoreCase("null")) return Optional.empty();
            return Optional.of(answer);
        } catch (Exception e) {
            log.error("extractSingleFact [{}]: помилка", factName, e);
            return Optional.empty();
        }
    }

//    private Map<String, Object> extractCriticalFacts(List<ChatMessage> messages,
//                                                     Map<String, Object> existingFacts) {
//        String historyText = messages.stream()
//                .map(m -> m.getRole() + ": " + m.getContent())
//                .collect(Collectors.joining("\n"));
//
//        String existingFactsJson = "{}";
//        if (existingFacts != null && !existingFacts.isEmpty()) {
//            try {
//                existingFactsJson = objectMapper.writeValueAsString(existingFacts);
//            } catch (Exception ignored) {}
//        }
//
//        String prompt = """
//        Ти екстрактор даних. Витягни факти з розмови у JSON форматі.
//        Існуючі факти (merge, не видаляй якщо не змінились): %s
//
//        ОБОВ'ЯЗКОВО шукай і заповнюй якщо є в розмові:
//        - size: розмір одягу (XS/S/M/L/XL) — користувач згадав розмір?
//        - budget: максимальний бюджет числом
//        - preferred_styles: список стилів/типів одягу
//        - cart_items: список id товарів що додали в кошик (шукай id в посиланнях /products/ID)
//        - in_stock_only: true якщо користувач хоче тільки в наявності
//        - body_params: {height, weight} якщо згадувались
//
//        Розмова:
//        %s
//
//        Відповідь ТІЛЬКИ валідний JSON без пояснень і markdown.
//        """.formatted(existingFactsJson, historyText);
//
//        try {
//            String raw = ollamaChatModel.call(prompt).trim()
//                    .replaceAll("```json|```", "").trim();
//            return objectMapper.readValue(raw, new TypeReference<>() {});
//        } catch (Exception e) {
//            log.error("ContextService: помилка extractCriticalFacts", e);
//            return existingFacts != null ? existingFacts : Collections.emptyMap();
//        }
//    }
}