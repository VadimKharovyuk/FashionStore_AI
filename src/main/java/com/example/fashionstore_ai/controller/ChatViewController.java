package com.example.fashionstore_ai.controller;

import com.example.fashionstore_ai.config.SessionResolver;
import com.example.fashionstore_ai.dto.chat.ChatMessageResponse;
import com.example.fashionstore_ai.dto.chat.ChatSessionResponse;
import com.example.fashionstore_ai.service.ChatService;
import com.example.fashionstore_ai.util.MarkdownUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        List<ChatMessageResponse> messages = session != null ? session.messages() : List.of();

        List<Map<String, Object>> renderedMessages = messages.stream()
                .map(msg -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("role", msg.role());
                    m.put("agentType", msg.agentType());
                    m.put("createdAt", msg.createdAt());
                    m.put("renderedContent", MarkdownUtils.render(msg.content()));
                    return m;
                })
                .toList();

        model.addAttribute("sessionId", sessionId);
        model.addAttribute("messages", renderedMessages);
        model.addAttribute("hasSummary", session != null && session.hasSummary());

        return "client/chat";
    }
}