-- Спочатку extension (якщо ще немає)
CREATE EXTENSION IF NOT EXISTS vector;

-- Додаємо vector колонку в agent_registry
ALTER TABLE agent_registry
    ADD COLUMN IF NOT EXISTS embedding vector(1024);

-- Додаємо vector колонку в tool_registry
ALTER TABLE tool_registry
    ADD COLUMN IF NOT EXISTS embedding vector(1024);

-- Індекси для швидкого пошуку
CREATE INDEX IF NOT EXISTS agent_registry_embedding_idx
    ON agent_registry
        USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 10);

CREATE INDEX IF NOT EXISTS tool_registry_embedding_idx
    ON tool_registry
        USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 10);