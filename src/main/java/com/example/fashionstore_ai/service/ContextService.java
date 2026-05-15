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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContextService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final OllamaChatModel ollamaChatModel;

    private static final int HISTORY_SIZE        = 20;
    private static final int PINNED_COUNT        = 3;
    private static final int SUMMARY_THRESHOLD   = 30;
    private static final int KEEP_RECENT         = 15;

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
    // Розбито на 3 методи щоб БД-з'єднання не трималось поки Ollama думає

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

        // Крок 2: генеруємо summary БЕЗ відкритої транзакції
        // Ollama може думати 30-60с — з'єднання з БД вільне весь цей час
        String newSummary = generateSummary(toSummarize, session.getSummary());
        if (newSummary.isBlank()) return;

        // Крок 3: зберігаємо результат → коротка нова транзакція
        saveSummary(session, newSummary, summarizeUpTo);
    }

    @Transactional(readOnly = true)
    public boolean needsSummarization(ChatSession session) {
        return chatMessageRepository.countByChatSessionId(session.getId()) > SUMMARY_THRESHOLD;
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getMessagesForSummarization(ChatSession session) {
        long total        = chatMessageRepository.countByChatSessionId(session.getId());
        int  summarizeUpTo = (int) (total - KEEP_RECENT);
        return chatMessageRepository.findForSummarization(session.getId(), summarizeUpTo);
    }

    @Transactional
    public void saveSummary(ChatSession session, String summary, int summarizedCount) {
        session.setSummary(summary);
        session.setSummaryUpdatedAt(LocalDateTime.now());
        session.setSummarizedMessagesCount(summarizedCount);
        chatSessionRepository.save(session);
        log.info("ContextService: summary збережено ({} символів)", summary.length());
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
}