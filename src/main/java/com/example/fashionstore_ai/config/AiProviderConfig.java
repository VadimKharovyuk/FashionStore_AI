package com.example.fashionstore_ai.config;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;


@Configuration
public class AiProviderConfig {

    // ── LOCAL: Ollama ─────────────────────────────────────────
    @Configuration
    @Profile("local")
    static class OllamaConfig {

        @Bean
        @Primary
        public EmbeddingModel embeddingModel(OllamaEmbeddingModel ollamaEmbeddingModel) {
            return ollamaEmbeddingModel;
        }
    }

    // ── PROD: OpenRouter ──────────────────────────────────────
    @Configuration
    @Profile("openai")
    static class OpenAiConfig {

        @Bean
        @Primary
        public EmbeddingModel embeddingModel(OpenAiEmbeddingModel openAiEmbeddingModel) {
            return openAiEmbeddingModel;
        }
    }
}
