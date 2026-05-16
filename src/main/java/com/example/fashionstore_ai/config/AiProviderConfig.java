package com.example.fashionstore_ai.config;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
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

        // ⚡ Быстрая модель — для Orchestrator и простых задач
        @Bean
        @Primary
        public ChatClient chatClient(OllamaChatModel ollamaChatModel) {
            return ChatClient.builder(ollamaChatModel).build();
        }

        // 🧠 Умная модель — для агентов где важны инструкции
        @Bean("smartChatClient")
        public ChatClient smartChatClient(OllamaApi ollamaApi) {
            OllamaChatModel smartModel = OllamaChatModel.builder()
                    .ollamaApi(ollamaApi)
                    .defaultOptions(OllamaChatOptions.builder()
                            .model("qwen3:8b")
                            .temperature(0.3)
                            .build())
                    .build();
            return ChatClient.builder(smartModel).build();
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

        // В prod оба клиента используют OpenRouter — просто один бин
        @Bean("smartChatClient")
        public ChatClient smartChatClient(OpenAiChatModel openAiChatModel) {
            return ChatClient.builder(openAiChatModel).build();
        }
    }
}