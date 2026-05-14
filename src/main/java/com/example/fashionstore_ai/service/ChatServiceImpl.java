package com.example.fashionstore_ai.service;

import com.example.fashionstore_ai.dto.chat.ChatMessageResponse;
import com.example.fashionstore_ai.dto.chat.ChatSessionResponse;
import com.example.fashionstore_ai.dto.chat.StreamChunk;
import com.example.fashionstore_ai.enums.AgentType;
import com.example.fashionstore_ai.mapper.ChatMapper;
import com.example.fashionstore_ai.model.ChatMessage;
import com.example.fashionstore_ai.model.ChatSession;
import com.example.fashionstore_ai.repository.ChatMessageRepository;
import com.example.fashionstore_ai.repository.ChatSessionRepository;
import com.example.fashionstore_ai.service.ChatService;
import com.example.fashionstore_ai.service.ContextService;
import com.example.fashionstore_ai.tools.OrchestratorAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ContextService contextService;
    private final OrchestratorAgent orchestratorAgent;
    private final ChatMapper chatMapper;

    // ── Streaming ─────────────────────────────────────────────────

    @Override
    public Flux<StreamChunk> chatStream(String sessionId, String userMessage) {
        log.info("ChatService.chatStream: sessionId={}", sessionId);

        return Flux.defer(() -> {
            // підготовка сесії і history
            ChatSession session = getOrCreateSession(sessionId);
            int messageIndex = (int) chatMessageRepository.countByChatSessionId(session.getId());
            contextService.saveUserMessage(session, userMessage, messageIndex);
            List<Message> history = contextService.buildHistory(session);

            StringBuilder fullResponse = new StringBuilder();

            return Flux.just(StreamChunk.session(sessionId))
                    .concatWith(
                            orchestratorAgent.chatStream(sessionId, userMessage, history)
                                    .map(token -> {
                                        fullResponse.append(token);
                                        return StreamChunk.token(token);
                                    })
                    )
                    .concatWith(Flux.defer(() -> {
                        try {
                            AgentType usedAgent = orchestratorAgent.route(userMessage);
                            ChatMessage saved = contextService.saveAssistantMessage(
                                    session, fullResponse.toString(),
                                    messageIndex + 1, usedAgent);
                            contextService.summarizeIfNeeded(session);
                            return Flux.just(StreamChunk.message(saved.getId()));
                        } catch (Exception e) {
                            log.warn("ChatService: не вдалось зберегти відповідь: {}", e.getMessage());
                            return Flux.empty();
                        }
                    }))
                    .concatWith(Flux.just(StreamChunk.done()))
                    .onErrorResume(e -> {
                        log.error("ChatService.chatStream error", e);
                        return Flux.just(StreamChunk.error(e.getMessage()), StreamChunk.done());
                    });
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // ── Non-streaming (для тестів) ────────────────────────────────

    @Override
    @Transactional
    public ChatMessageResponse chat(String sessionId, String userMessage) {
        log.info("ChatService.chat: sessionId={}", sessionId);

        ChatSession session = getOrCreateSession(sessionId);
        int messageIndex = (int) chatMessageRepository.countByChatSessionId(session.getId());

        contextService.saveUserMessage(session, userMessage, messageIndex);
        List<Message> history = contextService.buildHistory(session);

        String agentResponse = orchestratorAgent.chat(sessionId, userMessage, history);

        ChatMessage assistantMsg = contextService.saveAssistantMessage(
                session, agentResponse, messageIndex + 1, AgentType.SHOPPING_ASSISTANT);

        contextService.summarizeIfNeeded(session);

        return chatMapper.toResponse(assistantMsg);
    }

    // ── Session / messages ────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ChatSessionResponse getSession(String sessionId) {
        return chatSessionRepository
                .findBySessionIdAndStatus(sessionId, "ACTIVE")
                .map(chatMapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(String sessionId) {
        return chatSessionRepository
                .findBySessionIdAndStatus(sessionId, "ACTIVE")
                .map(session -> chatMapper.toResponseList(
                        chatMessageRepository.findByChatSessionIdOrderByCreatedAtAsc(session.getId())))
                .orElse(List.of());
    }

    // ── Private helpers ───────────────────────────────────────────

    private ChatSession getOrCreateSession(String sessionId) {
        return chatSessionRepository
                .findBySessionIdAndStatus(sessionId, "ACTIVE")
                .orElseGet(() -> {
                    ChatSession s = ChatSession.builder()
                            .sessionId(sessionId)
                            .status("ACTIVE")
                            .activeAgentType(AgentType.SHOPPING_ASSISTANT)
                            .summarizedMessagesCount(0)
                            .build();
                    ChatSession saved = chatSessionRepository.save(s);
                    log.info("ChatService: нова сесія sessionId={}", sessionId);
                    return saved;
                });
    }

    @Transactional
    protected void saveCompletedResponse(ChatSession session, String content, int messageIndex) {
        contextService.saveAssistantMessage(
                session, content, messageIndex, AgentType.SHOPPING_ASSISTANT);
        contextService.summarizeIfNeeded(session);
        log.debug("ChatService: збережено streaming відповідь ({} символів)", content.length());
    }
}