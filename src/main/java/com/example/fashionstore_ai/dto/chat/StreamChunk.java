package com.example.fashionstore_ai.dto.chat;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StreamChunk(
        String type,     // token | session | message | error | done
        String content,  // токен тексту (для type=token)
        String sessionId,// (для type=session)
        Long messageId   // (для type=message)
) {
    public static StreamChunk token(String content) {
        return new StreamChunk("token", content, null, null);
    }

    public static StreamChunk session(String sessionId) {
        return new StreamChunk("session", null, sessionId, null);
    }

    public static StreamChunk message(Long messageId) {
        return new StreamChunk("message", null, null, messageId);
    }

    public static StreamChunk error(String message) {
        return new StreamChunk("error", message, null, null);
    }

    public static StreamChunk done() {
        return new StreamChunk("done", null, null, null);
    }
}
