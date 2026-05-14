package com.example.fashionstore_ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.context.annotation.*;

@Configuration
public class AiProviderConfig {

    // ── LOCAL: Ollama ─────────────────────────────────────────────
    @Configuration
    @Profile("local")
    static class OllamaConfig {

        @Bean
        @Primary
        public EmbeddingModel embeddingModel(OllamaEmbeddingModel ollamaEmbeddingModel) {
            return ollamaEmbeddingModel;
        }

        @Bean
        @Primary
        public ChatClient chatClient(OllamaChatModel ollamaChatModel) {
            return ChatClient.builder(ollamaChatModel).build();
        }
    }

    // ── PROD: OpenRouter ──────────────────────────────────────────
    @Configuration
    @Profile("openai")
    static class OpenAiConfig {

        @Bean
        @Primary
        public EmbeddingModel embeddingModel(OpenAiEmbeddingModel openAiEmbeddingModel) {
            return openAiEmbeddingModel;
        }

        @Bean
        @Primary
        public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
            return ChatClient.builder(openAiChatModel).build();
        }
    }
}