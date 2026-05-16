package com.example.fashionstore_ai.tools;

import com.example.fashionstore_ai.config.BaseTool;
import com.example.fashionstore_ai.dto.product.ProductResponse;
import com.example.fashionstore_ai.enums.Category;
import com.example.fashionstore_ai.enums.Gender;
import com.example.fashionstore_ai.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationTool extends BaseTool {

    private final RecommendationService recommendationService;

    @Tool(name = "getPersonalizedRecommendations",
            description = """
                  Персональні рекомендації на основі переглянутих товарів.
                  Використовуй як відповідь на: "що порадиш?", "що мені підійде?",
                  "покажи щось цікаве", "рекомендації для мене",
                  "що одягнути на вечірку/зустріч/захід".
                  Якщо немає history — повертає bestsellers.
                  ВАЖЛИВО: передавай точний sessionId з системного промпту.
                  ВАЖЛИВО: якщо користувач вказує стать (чоловіча/жіноча вечірка, для чоловіка тощо) — передавай gender.
                  """)
    public String getPersonalizedRecommendations(
            @ToolParam(description = "ID сесії користувача — береться з системного промпту")
            String sessionId,

            @ToolParam(description = "Стать: передавай MEN якщо запит явно чоловічий, WOMEN якщо жіночий. Якщо стать невідома або не вказана — не передавай цей параметр (залиш null)", required = false)
            Gender gender,

            @ToolParam(description = "Кількість рекомендацій (за замовчуванням 5)", required = false)
            Integer limit
    ) {
        sessionId = normalizeSessionId(sessionId);
        log.info("Tool getPersonalizedRecommendations: sessionId={} gender={}", sessionId, gender);

        if (sessionId.isBlank()) {
            return "Помилка: sessionId порожній. Використовуй sessionId з системного промпту.";
        }

        int l = limit != null ? limit : 5;
        List<ProductResponse> products = recommendationService.getPersonalized(sessionId, gender, l);

        if (products.isEmpty()) return "На жаль, рекомендацій поки немає. Переглянь каталог!";
        return "🎯 Рекомендації:\n\n" + formatList(products) +
                "\n\n⚠️ Використовуй ТІЛЬКИ ці товари. Не додавай інших.";
    }

    @Tool(name = "getSimilarProducts",
            description = """
                  Схожі товари з тієї ж категорії.
                  Використовуй коли: користувач дивиться товар і питає
                  "є щось схоже?", "покажи альтернативи", "інші варіанти".
                  """)
    public String getSimilarProducts(
            @ToolParam(description = "ID товару")
            Long productId,

            @ToolParam(description = "Кількість (за замовчуванням 4)", required = false)
            Integer limit
    ) {
        log.info("Tool getSimilarProducts: productId={}", productId);
        int l = limit != null ? limit : 4;
        try {
            List<ProductResponse> products = recommendationService.getSimilar(productId, l);
            if (products.isEmpty()) return "Схожих товарів не знайдено.";
            return "👗 Схожі товари:\n\n" + formatList(products) +
                    "\n\n⚠️ Використовуй ТІЛЬКИ ці товари. Не додавай інших.";
        } catch (Exception e) {
            return "Товар з id=" + productId + " не знайдено.";
        }
    }

    @Tool(name = "getComplementaryProducts",
            description = """
                  Доповнюючі товари — що підходить до вже обраного.
                  Використовуй коли: користувач обрав товар і питає
                  "що до цього підійде?", "з чим носити?", "що ще взяти?".
                  """)
    public String getComplementaryProducts(
            @ToolParam(description = "Список ID товарів (через кому)")
            String productIdsStr,

            @ToolParam(description = "Кількість (за замовчуванням 4)", required = false)
            Integer limit
    ) {
        log.info("Tool getComplementaryProducts: productIds={}", productIdsStr);
        int l = limit != null ? limit : 4;
        try {
            List<Long> ids = List.of(productIdsStr.split(","))
                    .stream()
                    .map(String::trim)
                    .map(Long::parseLong)
                    .toList();
            List<ProductResponse> products = recommendationService.getComplementary(ids, l);
            if (products.isEmpty()) return "Доповнюючих товарів не знайдено.";
            return "✨ Чудово доповнить твій вибір:\n\n" + formatList(products) +
                    "\n\n⚠️ Використовуй ТІЛЬКИ ці товари. Не додавай інших.";
        } catch (Exception e) {
            return "Помилка при пошуку доповнюючих товарів: " + e.getMessage();
        }
    }

    @Tool(name = "getBestsellers",
            description = """
                  Хіти продажів магазину.
                  Використовуй коли: "що популярне?", "хіти", "топ товарів",
                  "що зараз купують?", "найкращі позиції".
                  """)
    public String getBestsellers(
            @ToolParam(description = "Категорія (опціонально): CASUAL, SPORT, EVENING і т.д.", required = false)
            Category category,

            @ToolParam(description = "Стать (опціонально): WOMEN, MEN, UNISEX", required = false)
            Gender gender,

            @ToolParam(description = "Кількість (за замовчуванням 5)", required = false)
            Integer limit
    ) {
        log.info("Tool getBestsellers: category={} gender={}", category, gender);
        int l = limit != null ? limit : 5;
        List<ProductResponse> products = recommendationService.getBestsellers(category, gender, l);
        if (products.isEmpty()) return "Хітів продажів не знайдено за вказаними фільтрами.";
        return "⭐ Хіти продажів:\n\n" + formatList(products) +
                "\n\n⚠️ Використовуй ТІЛЬКИ ці товари. Не додавай інших.";
    }

    @Tool(name = "getNewArrivals",
            description = """
                  Новинки магазину.
                  Використовуй коли: "що нового?", "нові надходження",
                  "нова колекція", "що нещодавно з'явилось?", "покажи новинки".
                  Якщо користувач не вказав категорію — НЕ передавай category (залиш null).
                  Якщо користувач не вказав стать — НЕ передавай gender (залиш null).
                  """)
    public String getNewArrivals(
            @ToolParam(description = "Категорія (опціонально)", required = false)
            Category category,

            @ToolParam(description = "Стать (опціонально)", required = false)
            Gender gender,

            @ToolParam(description = "Кількість (за замовчуванням 5)", required = false)
            Integer limit
    ) {
        log.info("Tool getNewArrivals: category={} gender={}", category, gender);
        int l = limit != null ? limit : 5;
        List<ProductResponse> products = recommendationService.getNewArrivals(category, gender, l);
        if (products.isEmpty()) return "Новинок за вказаними фільтрами не знайдено.";
        return "🆕 Нові надходження:\n\n" + formatList(products) +
                "\n\n⚠️ Використовуй ТІЛЬКИ ці товари. Не додавай інших.";
    }

    @Tool(name = "getViewHistory",
            description = """
                  Нещодавно переглянуті товари користувача.
                  Використовуй коли: "що я переглядав?", "покажи переглянуте",
                  "поверни до того що я дивився".
                  """)
    public String getViewHistory(
            @ToolParam(description = "ID сесії користувача — береться з системного промпту")
            String sessionId,

            @ToolParam(description = "Кількість (за замовчуванням 5)", required = false)
            Integer limit
    ) {
        sessionId = normalizeSessionId(sessionId);
        log.info("Tool getViewHistory: sessionId={}", sessionId);

        if (sessionId.isBlank()) {
            return "Помилка: sessionId порожній. Використовуй sessionId з системного промпту.";
        }

        int l = limit != null ? limit : 5;
        List<ProductResponse> products = recommendationService.getViewHistory(sessionId, l);
        if (products.isEmpty()) return "Історія переглядів порожня.";
        return "🕐 Нещодавно переглянуті:\n\n" + formatList(products) +
                "\n\n⚠️ Використовуй ТІЛЬКИ ці товари. Не додавай інших.";
    }

    // ── Format helper — з посиланнями та gender ───────────────────

    private String formatList(List<ProductResponse> products) {
        return products.stream()
                .map(p -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("[").append(p.name()).append("](/products/").append(p.id()).append(")")
                            .append(" | ").append(p.brand())
                            .append(" | $").append(p.discountedPrice())
                            .append(" | ").append(p.gender()); // явно показываем gender модели
                    if (p.discountPercent() != null && p.discountPercent() > 0) {
                        sb.append(" (-").append(p.discountPercent()).append("%)");
                    }
                    if (p.isBestseller()) sb.append(" ⭐");
                    if (p.isNew())        sb.append(" 🆕");
                    return sb.toString();
                })
                .collect(Collectors.joining("\n"));
    }
}