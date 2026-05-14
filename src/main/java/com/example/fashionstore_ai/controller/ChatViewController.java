package com.example.fashionstore_ai.controller;

import com.example.fashionstore_ai.config.SessionResolver;
import com.example.fashionstore_ai.dto.chat.ChatSessionResponse;
import com.example.fashionstore_ai.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatViewController {

    private final ChatService chatService;
    private final SessionResolver sessionResolver;

    @GetMapping("/chat")
    public String chatPage(HttpServletRequest request,
                           HttpServletResponse response,
                           Model model) {
        String sessionId = sessionResolver.resolve(request, response);

        ChatSessionResponse session = chatService.getSession(sessionId);
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("messages", session != null ? session.messages() : List.of());
        model.addAttribute("hasSummary", session != null && session.hasSummary());

        return "client/chat";
    }
}