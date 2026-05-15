package com.example.fashionstore_ai.config;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;


@Component
@Slf4j
public class SessionResolver {

    private static final String SESSION_COOKIE = "FASHION_SESSION";
    private static final int COOKIE_MAX_AGE = 60 * 60 * 24 * 30; // 30 днів

    public String resolve(HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() != null) {
            String existing = Arrays.stream(request.getCookies())
                    .filter(c -> SESSION_COOKIE.equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);

            if (existing != null && !existing.isBlank()) return existing;
        }

        String newSessionId = UUID.randomUUID().toString(); // завжди lowercase
        Cookie cookie = new Cookie(SESSION_COOKIE, newSessionId);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        log.info("SessionResolver: новий sessionId={}", newSessionId);
        return newSessionId;
    }

    // нормалізація sessionId що приходить від LLM
    public static String normalize(String sessionId) {
        if (sessionId == null) return "";
        return sessionId.toLowerCase()
                .replace("<", "")
                .replace(">", "")
                .trim();
    }
}