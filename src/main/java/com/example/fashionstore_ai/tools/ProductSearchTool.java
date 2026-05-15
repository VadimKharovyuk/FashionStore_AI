package com.example.fashionstore_ai.tools;


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
public class ProductSearchTool {

    private final ProductService productService;

    @Tool(name = "searchProducts",
            description = """
                  Пошук товарів у магазині за фільтрами. Всі параметри опціональні.
                  Використовуй коли: користувач шукає одяг, питає що є в наявності,
                  хоче знайти товар за категорією, кольором, ціною або іншими характеристиками.
                  Повертає список товарів з цінами, розмірами і наявністю.
                  """)
    public String searchProducts(
            @ToolParam(description = "Категорія: CASUAL, SPORT, CLASSIC, EVENING, BUSINESS, BEACH, PARTY, STREETWEAR, LOUNGEWEAR", required = false)
            Category category,

            @ToolParam(description = "Стать: WOMEN, MEN, UNISEX", required = false)
            Gender gender,

            @ToolParam(description = "Сезон: SUMMER, WINTER, SPRING_SUMMER, FALL_WINTER, ALL_SEASON", required = false)
            Season season,

            @ToolParam(description = "Колір: BLACK, WHITE, RED, BLUE, GREEN, BEIGE, PINK, BROWN, GREY, YELLOW, ORANGE, PURPLE, NAVY, OLIVE, MULTICOLOR, PRINT", required = false)
            Color color,

            @ToolParam(description = "Матеріал: COTTON, POLYESTER, WOOL, SILK, LINEN, DENIM, LEATHER, VISCOSE, CASHMERE, SYNTHETIC_MIX", required = false)
            Material material,

            @ToolParam(description = "Крій: SLIM, REGULAR, OVERSIZE, RELAXED, WIDE_LEG, CROP", required = false)
            FitType fitType,

            @ToolParam(description = "Максимальна ціна в USD", required = false)
            BigDecimal maxPrice
    ) {
        log.info("Tool searchProducts: category={} gender={} season={} color={} material={} fitType={} maxPrice={}",
                category, gender, season, color, material, fitType, maxPrice);

        List<ProductResponse> products = productService.search(
                category, gender, season, color, material, fitType, maxPrice);

        if (products.isEmpty()) {
            return "Товарів за вказаними фільтрами не знайдено. " +
                    "Спробуй розширити пошук — прибери деякі фільтри.";
        }

        return formatProductList(products);
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