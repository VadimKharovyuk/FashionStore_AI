```markdown
# FashionStore AI 🛍️

AI-powered fashion store with multi-agent system: shopping, sizing, support and recommendations.

## About

FashionStore AI — тестовий проект інтернет-магазину одягу з мульти-агентною AI системою на Spring Boot + Spring AI. Замість звичайного пошуку по фільтрам — розумні агенти які розуміють запити природною мовою.

## Агенти

| Агент | Що робить |
|---|---|
| 🎯 OrchestratorAgent | Визначає який агент потрібен і делегує запит |
| 🛍️ ShoppingAssistant | Знаходить одяг за описом, фільтрує, додає в кошик |
| 📏 SizingAgent | Підбирає розмір по параметрах тіла з урахуванням специфіки бренду |
| 🎧 SupportAgent | Трекінг замовлень, повернення, зміна адреси доставки |
| ✨ RecommendationAgent | Персональні рекомендації на основі історії переглядів |

## Як це працює

```
Користувач: "шукаю чорне плаття на весілля, бюджет $80, розмір M"
↓
OrchestratorAgent → Agent RAG → ShoppingAssistant
↓
searchProducts(category=EVENING, color=BLACK, size=M, maxPrice=80)
↓
"Знайшла 3 варіанти: ось фото, склад тканини, наявність на складі"
```

## Стек

| Компонент | Технологія |
|---|---|
| Backend | Java 21, Spring Boot 4 |
| AI Framework | Spring AI 2.0.0-M6 |
| LLM (local) | Ollama — qwen3:8b |
| LLM (prod) | OpenRouter — llama-3.3-70b-instruct |
| Embedding | mxbai-embed-large (local) / text-embedding-3-small (prod) |
| Database | PostgreSQL 15 + pgvector |
| Agent/Tool RAG | pgvector — семантичний пошук по реєстру агентів і tools |
| Frontend | Thymeleaf + Bootstrap 5 |
| Deploy | Railway |

## Архітектура

```
Request
↓
OrchestratorAgent
↓
Agent RAG (pgvector) → знаходить потрібного агента
↓
Tool RAG (pgvector) → inject тільки релевантних tools
↓
LLM відповідає з реальними даними з БД
```

**Agent RAG і Tool RAG** — замість передачі всіх агентів і tools в кожен запит система семантично шукає потрібні через pgvector. Детальніше: [Tool RAG на WebsCraft](https://webscraft.org/blog/tool-rag-scho-robiti-koli-u-agenta-zabagato-instrumentiv)

## Структура проекту

```
src/main/java/com/example/fashionstore_ai/
├── config/
│   └── AiProviderConfig.java        # Ollama / OpenRouter провайдери
├── entity/
│   ├── Product.java
│   ├── ProductSize.java
│   ├── SizingChart.java
│   ├── Cart.java / CartItem.java
│   ├── Order.java / OrderItem.java
│   ├── ChatSession.java / ChatMessage.java
│   ├── AgentRegistry.java           # Agent RAG реєстр
│   ├── ToolRegistry.java            # Tool RAG реєстр
│   ├── UserMeasurements.java
│   └── ViewHistory.java
├── enums/
│   ├── AgentType.java
│   ├── Category.java
│   ├── Size.java / Season.java
│   ├── Color.java / Material.java
│   ├── Gender.java / FitType.java
│   └── OrderStatus.java
├── agent/
│   ├── OrchestratorAgent.java
│   ├── ShoppingAssistant.java
│   ├── SizingAgent.java
│   ├── SupportAgent.java
│   └── RecommendationAgent.java
├── tools/
│   ├── ProductSearchTool.java
│   ├── CartTool.java
│   ├── SizingTool.java
│   ├── OrderTool.java
│   └── RecommendationTool.java
├── registry/
│   ├── AgentRegistryService.java    # Agent RAG
│   └── ToolRegistryService.java     # Tool RAG
└── service/
├── ChatService.java
├── ProductService.java
└── CartService.java
```

## Запуск локально

### Вимоги
- Java 21+
- Maven
- PostgreSQL 15+
- Ollama

### 1. Ollama — встановити моделі

```bash
ollama pull qwen3:8b
ollama pull mxbai-embed-large
ollama serve
```

### 2. PostgreSQL — створити БД і extension

```sql
CREATE DATABASE "FashionStore_AI";

\c FashionStore_AI

CREATE EXTENSION IF NOT EXISTS vector;
```

### 3. Запуск

```bash
mvn spring-boot:run -Dspring.profiles.active=local
```

Відкрий браузер: `http://localhost:1430`

## Deploy на Railway

Встанови env vars:

```
SPRING_PROFILES_ACTIVE=openai
DB_URL=jdbc:postgresql://...
DB_USERNAME=postgres
DB_PASSWORD=your_password
OPENAI_API_KEY=your_openrouter_key
```

## Пов'язані статті

Проект ілюструє концепції з серії статей про AI агентів на Spring Boot:

- [Tool RAG: динамічний inject tools](https://webscraft.org/blog/tool-rag-scho-robiti-koli-u-agenta-zabagato-instrumentiv)
- [Grounding і довіра до джерел](https://webscraft.org/blog/grounding-v-ai-agentah-scho-robiti-koli-tool-call-povernuv-ne-te)
- [Agent Chat: два AI агенти](https://webscraft.org/blog/agent-chat-dva-ai-agenti-scho-sperechayutsya-spring-boot-4-spring-ai-ollama-openrouter)
- [AskYourDocs](https://askyourdocs.org/en/) — production RAG система від того ж автора

## Автор

**Vadim Kharovyuk** — Java Backend розробник

- 🌐 [webscraft.org](https://webscraft.org)
- 💬 Telegram: [@name_lucky_lucky](https://t.me/name_lucky_lucky)
- 💻 GitHub: [VadimKharovyuk](https://github.com/VadimKharovyuk)

## Ліцензія

MIT License
```