package com.example.fashionstore_ai.config;

public abstract class BaseTool {

    // нормалізація sessionId що приходить від LLM
    // модель може передати у верхньому регістрі або з кутовими дужками
    protected String normalizeSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return "";
        return sessionId.toLowerCase()
                .replace("<", "")
                .replace(">", "")
                .trim();
    }
}