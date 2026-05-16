package com.example.fashionstore_ai.tools;
import com.example.fashionstore_ai.config.BaseTool;
import com.example.fashionstore_ai.dto.product.ProductResponse;
import com.example.fashionstore_ai.enums.*;
import com.example.fashionstore_ai.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductSearchTool extends BaseTool {

    private final ProductService productService;
    // ── Максимум спроб для розширення пошуку ─────────────────────
    private static final int MAX_SEARCH_RETRIES = 2;


    @Tool(name = "searchProducts",
            description = """
                  Пошук товарів у магазині за фільтрами. Всі параметри опціональні.
                  Використовуй коли: користувач шукає одяг, питає що є в наявності,
                  хоче знайти товар за категорією, кольором, ціною або іншими характеристиками.
                  Повертає JSON з полем "found": true/false.

                  СТРАТЕГІЯ ПРИ ПОРОЖНЬОМУ РЕЗУЛЬТАТІ (found=false):
                  - Спроба 1: прибери найбільш специфічний фільтр (color або material)
                  - Спроба 2: прибери ще один фільтр (залиш тільки category або gender)
                  - Після 2 спроб: повідом що нічого не знайдено, запитай чи змінити пошук
                  - НІКОЛИ не вигадуй товари яких немає у відповіді

                  Category: CASUAL, SPORT, CLASSIC, EVENING, BUSINESS, BEACH, PARTY, STREETWEAR, LOUNGEWEAR
                  Gender: WOMEN, MEN, UNISEX
                  Color: BLACK, WHITE, RED, BLUE, GREEN, BEIGE, PINK, BROWN, GREY, YELLOW, ORANGE, PURPLE, NAVY, OLIVE, MULTICOLOR, PRINT
                  Material: COTTON, POLYESTER, WOOL, SILK, LINEN, DENIM, LEATHER, VISCOSE, CASHMERE, SYNTHETIC_MIX
                  FitType: SLIM, REGULAR, OVERSIZE, RELAXED, WIDE_LEG, CROP
                  Якщо не впевнений у значенні — залиш параметр null!
                  """)
    public String searchProducts(
            @ToolParam(description = "Категорія: CASUAL, SPORT і т.д. null якщо не вказано", required = false)
            String category,

            @ToolParam(description = "Стать: WOMEN, MEN, UNISEX. null якщо не вказано", required = false)
            String gender,

            @ToolParam(description = "Сезон: SUMMER, WINTER, SPRING_SUMMER, FALL_WINTER, ALL_SEASON. null якщо не вказано", required = false)
            String season,

            @ToolParam(description = "Колір: BLACK, WHITE, RED і т.д. null якщо не вказано", required = false)
            String color,

            @ToolParam(description = "Матеріал: COTTON, LEATHER, WOOL і т.д. null якщо не вказано", required = false)
            String material,

            @ToolParam(description = "Крій: SLIM, REGULAR, OVERSIZE і т.д. null якщо не вказано", required = false)
            String fitType,

            @ToolParam(description = "Максимальна ціна в USD", required = false)
            BigDecimal maxPrice,

            @ToolParam(description = "Номер спроби пошуку (1 = перша, 2 = розширений пошук). За замовчуванням 1", required = false)
            Integer attempt
    ) {
        int currentAttempt = (attempt != null) ? attempt : 1;

        log.info("Tool searchProducts [attempt={}/{}]: category={} gender={} color={} material={} fitType={} maxPrice={}",
                currentAttempt, MAX_SEARCH_RETRIES, category, gender, color, material, fitType, maxPrice);

        List<ProductResponse> products = productService.search(
                parseEnum(Category.class, category),
                parseEnum(Gender.class, gender),
                parseEnum(Season.class, season),
                parseEnum(Color.class, color),
                parseEnum(Material.class, material),
                parseEnum(FitType.class, fitType),
                maxPrice);

        if (products.isEmpty()) {
            if (currentAttempt < MAX_SEARCH_RETRIES) {
                return "ПОШУК НЕ ДАВ РЕЗУЛЬТАТІВ (спроба " + currentAttempt + " з " + MAX_SEARCH_RETRIES + "). " +
                        "ОБОВ'ЯЗКОВО викличи searchProducts ще раз з attempt=" + (currentAttempt + 1) + " " +
                        "і прибери фільтр color або material. НЕ відповідай без повторного виклику tool.";
            } else {
                return "ПОШУК НЕ ДАВ РЕЗУЛЬТАТІВ після " + MAX_SEARCH_RETRIES + " спроб. " +
                        "Повідом користувача що нічого не знайдено і запитай чи змінити пошук. " +
                        "НЕ додавай жодних товарів від себе.";
            }
        }

        return "ЗНАЙДЕНО " + products.size() + " товарів:\n\n" + formatProductList(products);
    }

    @Tool(name = "getProductDetails",
            description = """
                  Отримати детальну інформацію про конкретний товар по його id.
                  Використовуй коли: користувач питає більше про певний товар,
                  хоче знати склад тканини, догляд, нотатки по розміру.
                  """)
    public String getProductDetails(
            @ToolParam(description = "ID товару")
            Long productId
    ) {
        log.info("Tool getProductDetails: productId={}", productId);

        try {
            ProductResponse p = productService.getById(productId);
            return formatProductDetails(p);
        } catch (Exception e) {
            return "Товар з id=" + productId + " не знайдено.";
        }
    }

    @Tool(name = "checkStock",
            description = """
                  Перевірити наявність конкретного розміру товару на складі.
                  Використовуй коли: користувач питає чи є певний розмір,
                  перед додаванням в кошик щоб підтвердити наявність.
                  Повертає кількість одиниць на складі.
                  """)
    public String checkStock(
            @ToolParam(description = "ID товару")
            Long productId,

            @ToolParam(description = "Розмір: XS, S, M, L, XL, XXL")
            Size size
    ) {
        log.info("Tool checkStock: productId={} size={}", productId, size);

        int qty = productService.checkStock(productId, size);

        if (qty == 0) {
            return "Розмір " + size + " для товару id=" + productId +
                    " відсутній на складі. Рекомендую запропонувати інший розмір.";
        }

        return "Розмір " + size + " є в наявності: " + qty + " шт.";
    }

    // ── Safe enum parser — null якщо невідоме значення ──────────
    // ── Алиасы для Category ───────────────────────────────────────────
    private static final Map<String, String> CATEGORY_ALIASES = Map.ofEntries(
            Map.entry("OUTERWEAR",  "CLASSIC"),
            Map.entry("JACKET",     "CASUAL"),
            Map.entry("COAT",       "CLASSIC"),
            Map.entry("TOPS",       "CASUAL"),
            Map.entry("DRESSES",    "EVENING"),
            Map.entry("ACTIVEWEAR", "SPORT"),
            Map.entry("SWIMWEAR",   "BEACH"),
            Map.entry("КУРТКА",     "CASUAL"),
            Map.entry("ПАЛЬТО",     "CLASSIC"),
            Map.entry("СУКНЯ",      "EVENING"),
            Map.entry("СПОРТ",      "SPORT")
    );

    // ── Алиасы для Color ──────────────────────────────────────────────
    private static final Map<String, String> COLOR_ALIASES = Map.ofEntries(
            Map.entry("KHAKI",      "OLIVE"),
            Map.entry("CREAM",      "BEIGE"),
            Map.entry("IVORY",      "BEIGE"),
            Map.entry("DARK_BLUE",  "NAVY"),
            Map.entry("DARK BLUE",  "NAVY"),
            Map.entry("LIGHT_BLUE", "BLUE"),
            // українські
            Map.entry("ЗЕЛЕНИЙ",    "GREEN"),
            Map.entry("ЗЕЛЕНА",     "GREEN"),
            Map.entry("ЧОРНИЙ",     "BLACK"),
            Map.entry("ЧОРНА",      "BLACK"),
            Map.entry("БІЛИЙ",      "WHITE"),
            Map.entry("БІЛА",       "WHITE"),
            Map.entry("СИНІЙ",      "BLUE"),
            Map.entry("СИНЯ",       "BLUE"),
            Map.entry("ЧЕРВОНИЙ",   "RED"),
            Map.entry("ЧЕРВОНА",    "RED"),
            Map.entry("БЕЖЕВИЙ",    "BEIGE"),
            Map.entry("СІРИЙ",      "GREY"),
            Map.entry("РОЖЕВИЙ",    "PINK")
    );

    // ── Алиасы для Gender ─────────────────────────────────────────────
    private static final Map<String, String> GENDER_ALIASES = Map.ofEntries(
            Map.entry("MALE",       "MEN"),
            Map.entry("FEMALE",     "WOMEN"),
            Map.entry("WOMAN",      "WOMEN"),
            Map.entry("MAN",        "MEN"),
            // українські
            Map.entry("ЧОЛОВІЧИЙ",  "MEN"),
            Map.entry("ЧОЛОВІЧА",   "MEN"),
            Map.entry("ЖІНОЧИЙ",    "WOMEN"),
            Map.entry("ЖІНОЧА",     "WOMEN"),
            Map.entry("УНІСЕКС",    "UNISEX")
    );

    // ── Алиасы для FitType ────────────────────────────────────────────
    private static final Map<String, String> FIT_ALIASES = Map.ofEntries(
            Map.entry("LOOSE",      "RELAXED"),
            Map.entry("BAGGY",      "OVERSIZE"),
            Map.entry("FITTED",     "SLIM"),
            Map.entry("STRAIGHT",   "REGULAR"),
            Map.entry("CROPPED",    "CROP"),
            // українські
            Map.entry("ВІЛЬНИЙ",    "RELAXED"),
            Map.entry("ПРИТАЛЕНИЙ", "SLIM"),
            Map.entry("ОВЕРСАЙЗ",   "OVERSIZE"),
            Map.entry("ШИРОКИЙ",    "WIDE_LEG"),
            Map.entry("ВКОРОЧЕНИЙ", "CROP")
    );
    private static final Map<String, String> MATERIAL_ALIASES = Map.of(
            // українські
            "ШКІРЯНА",   "LEATHER",
            "ШКІРА",     "LEATHER",
            "ШКІРНА",    "LEATHER",
            "ВОВНЯНА",   "WOOL",
            "ВОВНА",     "WOOL",
            "БАВОВНА",   "COTTON",
            "БАВОВНЯНА", "COTTON",
            "ДЕНІМ",     "DENIM",
            "ШОВК",      "SILK",
            "ШОВКОВА",   "SILK"
    );

    // ── Safe enum parser з алиасами ───────────────────────────────────
    private <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value) {
        if (value == null || value.isBlank()) return null;

        String normalized = value.trim().toUpperCase();

        if (enumClass == Category.class) {
            normalized = CATEGORY_ALIASES.getOrDefault(normalized, normalized);
        } else if (enumClass == Color.class) {
            normalized = COLOR_ALIASES.getOrDefault(normalized, normalized);
        } else if (enumClass == Material.class) {
            normalized = MATERIAL_ALIASES.getOrDefault(normalized, normalized);
        } else if (enumClass == Gender.class) {
            normalized = GENDER_ALIASES.getOrDefault(normalized, normalized);
        } else if (enumClass == FitType.class) {
            normalized = FIT_ALIASES.getOrDefault(normalized, normalized);
        }

        try {
            return Enum.valueOf(enumClass, normalized);
        } catch (IllegalArgumentException e) {
            log.warn("Tool searchProducts: невідоме значення '{}' для {}, ігноруємо",
                    value, enumClass.getSimpleName());
            return null;
        }
    }

    // ── Format helpers ────────────────────────────────────────────

    private String formatProductList(List<ProductResponse> products) {
        StringBuilder sb = new StringBuilder();
        sb.append("Знайдено товарів: ").append(products.size()).append("\n\n");

        products.forEach(p -> {
            // посилання на сторінку товару
            sb.append("[").append(p.name()).append("](/products/").append(p.id()).append(")")
                    .append(" | ").append(p.brand());
            sb.append(" | $").append(p.discountedPrice());
            if (p.discountPercent() != null && p.discountPercent() > 0) {
                sb.append(" (знижка ").append(p.discountPercent()).append("%)");
            }
            sb.append("\n");
            sb.append("Категорія: ").append(p.category())
                    .append(" | Стать: ").append(p.gender()).append("\n");
            sb.append("Колір: ").append(p.colorDescription() != null
                    ? p.colorDescription() : p.color().name()).append("\n");
            sb.append("Розміри: ").append(formatSizes(p.availableSizes())).append("\n");
            if (p.isNew()) sb.append("🆕 Новинка\n");
            if (p.isBestseller()) sb.append("⭐ Хіт продажів\n");
            sb.append("---\n");
        });

        return sb.toString();
    }

    private String formatProductDetails(ProductResponse p) {
        StringBuilder sb = new StringBuilder();
        sb.append("Товар ID: ").append(p.id()).append("\n");
        sb.append("Назва: ").append(p.name()).append("\n");
        sb.append("Бренд: ").append(p.brand()).append("\n");
        sb.append("Артикул: ").append(p.sku()).append("\n");
        sb.append("Ціна: $").append(p.discountedPrice());
        if (p.discountPercent() > 0) {
            sb.append(" (знижка ").append(p.discountPercent()).append("%)");
        }
        sb.append("\n");
        sb.append("Категорія: ").append(p.category()).append("\n");
        sb.append("Стать: ").append(p.gender()).append("\n");
        sb.append("Сезон: ").append(p.season()).append("\n");
        sb.append("Колір: ").append(p.colorDescription() != null
                ? p.colorDescription() : p.color().name()).append("\n");
        sb.append("Матеріал: ").append(p.material()).append("\n");
        if (p.fabricComposition() != null) {
            sb.append("Склад: ").append(p.fabricComposition()).append("\n");
        }
        sb.append("Крій: ").append(p.fitType()).append("\n");
        if (p.styleNotes() != null) {
            sb.append("Нотатки по розміру: ").append(p.styleNotes()).append("\n");
        }
        if (p.careInstructions() != null) {
            sb.append("Догляд: ").append(p.careInstructions()).append("\n");
        }
        sb.append("Доступні розміри: ").append(formatSizes(p.availableSizes())).append("\n");
        sb.append("Виробництво: ").append(p.countryOfOrigin()).append("\n");
        if (p.description() != null) {
            sb.append("Опис: ").append(p.description()).append("\n");
        }
        return sb.toString();
    }

    private String formatSizes(Map<Size, Integer> sizes) {
        if (sizes == null || sizes.isEmpty()) return "немає в наявності";
        return sizes.entrySet().stream()
                .map(e -> e.getKey() + "(" + e.getValue() + "шт)")
                .collect(Collectors.joining(", "));
    }
}