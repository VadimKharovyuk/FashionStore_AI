package com.example.fashionstore_ai.config;

import com.example.fashionstore_ai.enums.AgentType;
import com.example.fashionstore_ai.model.AgentRegistry;
import com.example.fashionstore_ai.repository.AgentRegistryRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(3)
public class AgentRegistryInitializer implements ApplicationRunner {

    private final AgentRegistryRepository agentRegistryRepository;
    private final EmbeddingModel embeddingModel;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (agentRegistryRepository.count() > 0) {
            log.info("AgentRegistryInitializer: дані вже є, пропускаємо");
            return;
        }

        log.info("AgentRegistryInitializer: реєструємо агентів...");

        registerAgent(AgentRegistry.builder()
                .agentType(AgentType.SHOPPING_ASSISTANT)
                .displayName("Помічник з підбору одягу")
                .description("Агент для пошуку товарів, підбору одягу та управління кошиком. " +
                        "Допомагає знайти одяг за категорією, кольором, матеріалом, ціною та сезоном. " +
                        "Перевіряє наявність розмірів і додає товари в кошик.")
                .competencies("пошук товарів, фільтрація каталогу, перевірка наявності, " +
                        "додавання в кошик, видалення з кошика, перегляд кошика, " +
                        "підбір за кольором матеріалом ціною категорією сезоном")
                .triggerExamples("шукаю сукню, є чорні джинси, покажи спортивний одяг, " +
                        "додай в кошик, що є в наявності, знайди до 50 доларів, " +
                        "хочу купити, є розмір M, покажи кошик")
                .notResponsibleFor("підбір розміру по параметрах тіла, замовлення доставка повернення")
                .beanName("shoppingAssistant")
                .priority(1)
                .isActive(true)
                .build());

        registerAgent(AgentRegistry.builder()
                .agentType(AgentType.SIZING_AGENT)
                .displayName("Помічник з підбору розміру")
                .description("Агент для підбору розміру одягу на основі параметрів тіла. " +
                        "Зберігає виміри користувача і порівнює їх з розмірними сітками брендів. " +
                        "Враховує особливості крою різних брендів.")
                .competencies("підбір розміру, розмірна сітка бренду, збереження параметрів тіла, " +
                        "порівняння вимірів, рекомендація розміру, груди талія стегна зріст")
                .triggerExamples("який розмір мені підійде, груди 88 талія 68, розмірна сітка Zara, " +
                        "маломірить чи ні, який розмір взяти, мої параметри, виміри тіла, " +
                        "підбери розмір, розмір в H&M Mango Reserved")
                .notResponsibleFor("пошук товарів, кошик, замовлення")
                .beanName("sizingAgent")
                .priority(2)
                .isActive(true)
                .build());

        registerAgent(AgentRegistry.builder()
                .agentType(AgentType.SUPPORT_AGENT)
                .displayName("Служба підтримки")
                .description("Агент підтримки для роботи із замовленнями, доставкою і поверненнями. " +
                        "Відстежує статус замовлень, допомагає скасувати або змінити замовлення, " +
                        "оформлює повернення і обміни товарів.")
                .competencies("статус замовлення, трекінг доставки, скасування замовлення, " +
                        "зміна адреси доставки, повернення товару, обмін, політика повернень, " +
                        "де посилка, номер замовлення ORD")
                .triggerExamples("де моє замовлення, хочу скасувати, не прийшла посилка, " +
                        "повернути товар, змінити адресу, трекінг номер, статус доставки, " +
                        "замовлення ORD-2026, повернення обмін")
                .notResponsibleFor("пошук товарів, підбір розміру, кошик")
                .beanName("supportAgent")
                .priority(3)
                .isActive(true)
                .build());

        registerAgent(AgentRegistry.builder()
                .agentType(AgentType.RECOMMENDATION_AGENT)
                .displayName("Персональний стиліст")
                .description("Агент персональних рекомендацій на основі переглядів і вподобань. " +
                        "Підбирає товари виходячи з історії переглядів, пропонує схожі товари " +
                        "і доповнення до вже обраного. Показує хіти і новинки.")
                .competencies("персональні рекомендації, схожі товари, доповнюючі товари, " +
                        "хіти продажів, новинки, з чим носити, що порадиш, " +
                        "підбір образу, стиль, історія переглядів")
                .triggerExamples("що порадиш, покажи щось цікаве, що популярне, новинки, " +
                        "що до цього підійде, схожі товари, персональні рекомендації, " +
                        "підбери образ, що нового в магазині, хіти")
                .notResponsibleFor("пошук по фільтрах, підбір розміру, замовлення")
                .beanName("recommendationAgent")
                .priority(4)
                .isActive(true)
                .build());

        log.info("AgentRegistryInitializer: зареєстровано {} агентів",
                agentRegistryRepository.count());
    }


    private void registerAgent(AgentRegistry agent) {
        String textForEmbedding = agent.getDescription() + " " +
                agent.getCompetencies() + " " +
                agent.getTriggerExamples();

        float[] embedding = embeddingModel.embed(textForEmbedding);

        // конвертуємо float[] у рядок формату [0.1,0.2,...] для pgvector
        String vectorStr = Arrays.stream(toDoubleArray(embedding))
                .mapToObj(Double::toString)
                .collect(Collectors.joining(",", "[", "]"));

        // зберігаємо базові поля через JPA
        agentRegistryRepository.save(agent);

        // оновлюємо embedding через нативний SQL з явним кастом до vector
        entityManager.createNativeQuery(
                        "UPDATE agent_registry SET embedding = CAST(:vec AS vector) WHERE agent_type = :type")
                .setParameter("vec", vectorStr)
                .setParameter("type", agent.getAgentType().name())
                .executeUpdate();

        log.info("AgentRegistryInitializer: зареєстровано {} (embedding: {} dims)",
                agent.getAgentType(), embedding.length);
    }

    private double[] toDoubleArray(float[] floats) {
        double[] doubles = new double[floats.length];
        for (int i = 0; i < floats.length; i++) doubles[i] = floats[i];
        return doubles;
    }
}