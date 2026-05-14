package com.example.fashionstore_ai.controller.rest;

import com.example.fashionstore_ai.config.SessionResolver;
import com.example.fashionstore_ai.dto.chat.ChatMessageRequest;
import com.example.fashionstore_ai.dto.chat.ChatMessageResponse;
import com.example.fashionstore_ai.dto.chat.StreamChunk;
import com.example.fashionstore_ai.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatRestController {

    private final ChatService chatService;
    private final SessionResolver sessionResolver;

    // ── SSE: стрімінг з типізованими chunk-ами ───────────────────

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<StreamChunk> stream(
            @Valid @RequestBody ChatMessageRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String sessionId = sessionResolver.resolve(httpRequest, httpResponse);
        log.info("ChatRestController.stream: sessionId={}", sessionId);
        return chatService.chatStream(sessionId, request.message());
    }

    // ── POST: звичайна відповідь (без стрімінгу) ─────────────────

    @PostMapping
    public ResponseEntity<ChatMessageResponse> send(
            @Valid @RequestBody ChatMessageRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String sessionId = sessionResolver.resolve(httpRequest, httpResponse);
        log.info("ChatRestController.send: sessionId={}", sessionId);
        return ResponseEntity.ok(chatService.chat(sessionId, request.message()));
    }

    // ── GET: всі повідомлення сесії ───────────────────────────────

    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessageResponse>> messages(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String sessionId = sessionResolver.resolve(request, response);
        return ResponseEntity.ok(chatService.getMessages(sessionId));
    }
}