package com.example.fashionstore_ai.service;

import com.example.fashionstore_ai.dto.chat.ChatMessageResponse;
import com.example.fashionstore_ai.dto.chat.ChatSessionResponse;
import com.example.fashionstore_ai.dto.chat.StreamChunk;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatService {

    // стрімінг відповіді токен за токеном (для SSE)
    Flux<StreamChunk> chatStream(String sessionId, String userMessage);

    // звичайний запит без стрімінгу (для тестів / API)
    ChatMessageResponse chat(String sessionId, String userMessage);

    // отримати сесію з усіма повідомленнями (для UI при завантаженні)
    ChatSessionResponse getSession(String sessionId);

    // отримати тільки повідомлення
    List<ChatMessageResponse> getMessages(String sessionId);
    // повне скидання сесії
    void resetSession(String sessionId);


    ///clearHistory — видаляє повідомлення, сесія залишається активною. Summary зберігається — агент пам'ятає контекст. Для користувача — чистий чат але агент "пам'ятає" попередні покупки.
    void clearHistory(String sessionId);
}