package com.example.fashionstore_ai.tools;

import com.example.fashionstore_ai.config.BaseTool;
import com.example.fashionstore_ai.dto.userMeasurement.SizeRecommendation;
import com.example.fashionstore_ai.dto.userMeasurement.SizingChartResponse;
import com.example.fashionstore_ai.dto.userMeasurement.UserMeasurementsDto;
import com.example.fashionstore_ai.enums.FitType;
import com.example.fashionstore_ai.enums.Gender;
import com.example.fashionstore_ai.service.SizingService;
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
public class SizingTool extends BaseTool {

    private final SizingService sizingService;

    @Tool(name = "saveUserMeasurements",
            description = """
                  Зберегти параметри тіла користувача для підбору розміру.
                  Використовуй коли: користувач назвав параметри тіла (груди, талія, стегна, зріст).
                  Всі параметри опціональні — зберігай тільки те що назвав користувач.
                  Параметри зберігаються між сесіями — не питай знову якщо вже є.
                  """)
    public String saveUserMeasurements(
            @ToolParam(description = "ID сесії користувача")
            String sessionId,

            @ToolParam(description = "Обхват грудей в см", required = false)
            Integer chest,

            @ToolParam(description = "Обхват талії в см", required = false)
            Integer waist,

            @ToolParam(description = "Обхват стегон в см", required = false)
            Integer hips,

            @ToolParam(description = "Зріст в см", required = false)
            Integer height,

            @ToolParam(description = "Вага в кг (опціонально)", required = false)
            Integer weight,

            @ToolParam(description = "Бажаний крій: SLIM, REGULAR, OVERSIZE, RELAXED", required = false)
            FitType preferredFit
    ) {
        log.info("Tool saveUserMeasurements: sessionId={} chest={} waist={} hips={} height={}",
                sessionId, chest, waist, hips, height);

        UserMeasurementsDto saved = sizingService.saveMeasurements(
                sessionId, chest, waist, hips, height, weight, preferredFit);

        StringBuilder sb = new StringBuilder("✅ Параметри збережено:\n");
        if (saved.chest()  != null) sb.append("• Груди: ").append(saved.chest()).append("см\n");
        if (saved.waist()  != null) sb.append("• Талія: ").append(saved.waist()).append("см\n");
        if (saved.hips()   != null) sb.append("• Стегна: ").append(saved.hips()).append("см\n");
        if (saved.height() != null) sb.append("• Зріст: ").append(saved.height()).append("см\n");
        if (saved.preferredFit() != null) sb.append("• Крій: ").append(saved.preferredFit()).append("\n");
        return sb.toString();
    }

    @Tool(name = "getUserMeasurements",
            description = """
                  Отримати збережені параметри тіла користувача.
                  Використовуй перед підбором розміру щоб перевірити чи є вже дані.
                  Якщо даних немає — попроси користувача їх надати.
                  """)
    public String getUserMeasurements(
            @ToolParam(description = "ID сесії користувача")
            String sessionId
    ) {
        log.info("Tool getUserMeasurements: sessionId={}", sessionId);

        UserMeasurementsDto m = sizingService.getMeasurements(sessionId);

        if (m == null || !m.hasEnoughData()) {
            return "Параметри тіла не знайдено. Попроси користувача вказати: " +
                    "груди, талію і стегна в сантиметрах.";
        }

        StringBuilder sb = new StringBuilder("Збережені параметри користувача:\n");
        if (m.chest()  != null) sb.append("• Груди: ").append(m.chest()).append("см\n");
        if (m.waist()  != null) sb.append("• Талія: ").append(m.waist()).append("см\n");
        if (m.hips()   != null) sb.append("• Стегна: ").append(m.hips()).append("см\n");
        if (m.height() != null) sb.append("• Зріст: ").append(m.height()).append("см\n");
        if (m.preferredFit() != null) sb.append("• Крій: ").append(m.preferredFit()).append("\n");
        return sb.toString();
    }

    @Tool(name = "getSizingChart",
            description = """
                  Отримати повну розмірну сітку бренду.
                  Використовуй коли: користувач хоче побачити таблицю розмірів,
                  або коли потрібно пояснити різницю між розмірами бренду.
                  """)
    public String getSizingChart(
            @ToolParam(description = "Назва бренду: Zara, H&M, Mango, Massimo Dutti, Reserved")
            String brand,

            @ToolParam(description = "Стать: WOMEN, MEN, UNISEX")
            Gender gender
    ) {
        log.info("Tool getSizingChart: brand={} gender={}", brand, gender);

        List<SizingChartResponse> charts = sizingService.getSizingChart(brand, gender);

        if (charts.isEmpty()) {
            return "Розмірна сітка для " + brand + " (" + gender + ") не знайдена. " +
                    "Доступні бренди: " + String.join(", ", sizingService.getAvailableBrands());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Розмірна сітка ").append(brand).append(" (").append(gender).append("):\n\n");

        charts.forEach(c -> {
            sb.append("Розмір ").append(c.size()).append(":\n");
            if (c.chestMin() != null) sb.append("  Груди: ").append(c.chestMin()).append("-").append(c.chestMax()).append("см\n");
            if (c.waistMin() != null) sb.append("  Талія: ").append(c.waistMin()).append("-").append(c.waistMax()).append("см\n");
            if (c.hipMin()   != null) sb.append("  Стегна: ").append(c.hipMin()).append("-").append(c.hipMax()).append("см\n");
            sb.append("---\n");
        });

        if (!charts.isEmpty() && charts.get(0).fitNotes() != null) {
            sb.append("\n📌 ").append(charts.get(0).fitNotes());
        }

        return sb.toString();
    }

    @Tool(name = "recommendSize",
            description = """
                  Підібрати розмір для конкретного бренду на основі параметрів тіла.
                  Використовуй коли: користувач питає який розмір йому підійде в певному бренді.
                  Перед викликом переконайся що параметри тіла збережені через getUserMeasurements.
                  Повертає рекомендацію з поясненням і нотатками бренду.
                  """)
    public String recommendSize(
            @ToolParam(description = "ID сесії користувача")
            String sessionId,

            @ToolParam(description = "Назва бренду: Zara, H&M, Mango, Massimo Dutti, Reserved")
            String brand,

            @ToolParam(description = "Стать: WOMEN, MEN, UNISEX")
            Gender gender
    ) {
        log.info("Tool recommendSize: sessionId={} brand={} gender={}", sessionId, brand, gender);

        SizeRecommendation rec = sizingService.recommend(sessionId, brand, gender);

        if (rec.recommendedSize() == null) {
            return rec.explanation();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🎯 Рекомендований розмір: **").append(rec.recommendedSize()).append("**\n\n");
        sb.append(rec.explanation()).append("\n");

        if (rec.alternativeSize() != null) {
            sb.append("\n💡 Альтернатива: ").append(rec.alternativeSize())
                    .append(" (якщо надаєш перевагу вільнішому крою)\n");
        }

        if (rec.fitNotes() != null) {
            sb.append("\n📌 ").append(rec.fitNotes()).append("\n");
        }

        sb.append("\n✨ Впевненість: ").append(rec.confidence());
        return sb.toString();
    }

    @Tool(name = "getAvailableBrands",
            description = """
                  Отримати список брендів для яких є розмірна сітка в системі.
                  Використовуй коли: користувач питає для яких брендів можна підібрати розмір.
                  """)
    public String getAvailableBrands() {
        log.info("Tool getAvailableBrands");
        List<String> brands = sizingService.getAvailableBrands();
        return "Доступні бренди для підбору розміру: " + String.join(", ", brands);
    }
}