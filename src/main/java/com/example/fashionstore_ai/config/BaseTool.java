package com.example.fashionstore_ai.config;

//public abstract class BaseTool {
//
//    // нормалізація sessionId що приходить від LLM
//    // модель може передати у верхньому регістрі або з кутовими дужками
//    protected String normalizeSessionId(String sessionId) {
//        if (sessionId == null || sessionId.isBlank()) return "";
//        return sessionId.toLowerCase()
//                .replace("<", "")
//                .replace(">", "")
//                .trim();
//    }
//}

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public abstract class BaseTool {

    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

    private static final ThreadLocal<String> REAL_SESSION_ID = new ThreadLocal<>();

    public static void setCurrentSessionId(String sessionId) {
        REAL_SESSION_ID.set(sessionId != null ? sessionId.toLowerCase() : null);
    }

    public static void clearCurrentSessionId() {
        REAL_SESSION_ID.remove();
    }

    protected String normalizeSessionId(String sessionId) {
        String real = REAL_SESSION_ID.get();

        if (sessionId == null || sessionId.isBlank()) {
            log.warn("BaseTool: sessionId порожній — використовуємо реальний '{}'", real);
            return real != null ? real : "";
        }

        String normalized = sessionId.toLowerCase()
                .replace("<", "")
                .replace(">", "")
                .trim();

        // не валидный UUID — берём реальный
        if (!UUID_PATTERN.matcher(normalized).matches()) {
            log.warn("BaseTool: невалідний sessionId '{}' — використовуємо реальний '{}'",
                    sessionId, real);
            return real != null ? real : "";
        }

        // валидный UUID но не совпадает с реальным — подменяем
        if (real != null && !normalized.equals(real)) {
            log.warn("BaseTool: модель передала чужий sessionId '{}' — підміняємо на '{}'",
                    normalized, real);
            return real;
        }

        return normalized;
    }
}