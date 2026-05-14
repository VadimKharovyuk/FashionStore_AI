package com.example.fashionstore_ai.service;
import com.example.fashionstore_ai.dto.userMeasurement.SizeRecommendation;
import com.example.fashionstore_ai.dto.userMeasurement.SizingChartResponse;
import com.example.fashionstore_ai.dto.userMeasurement.UserMeasurementsDto;
import com.example.fashionstore_ai.enums.FitType;
import com.example.fashionstore_ai.enums.Gender;
import com.example.fashionstore_ai.enums.Size;
import com.example.fashionstore_ai.model.SizingChart;
import com.example.fashionstore_ai.model.UserMeasurements;
import com.example.fashionstore_ai.repository.SizingChartRepository;
import com.example.fashionstore_ai.repository.UserMeasurementsRepository;
import com.example.fashionstore_ai.service.SizingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SizingServiceImpl implements SizingService {

    private final UserMeasurementsRepository measurementsRepository;
    private final SizingChartRepository sizingChartRepository;

    // ── saveMeasurements ──────────────────────────────────────────

    @Override
    @Transactional
    public UserMeasurementsDto saveMeasurements(String sessionId,
                                                Integer chest,
                                                Integer waist,
                                                Integer hips,
                                                Integer height,
                                                Integer weight,
                                                FitType preferredFit) {
        UserMeasurements m = measurementsRepository
                .findBySessionId(sessionId)
                .orElseGet(() -> UserMeasurements.builder()
                        .sessionId(sessionId)
                        .preferredFit(FitType.REGULAR)
                        .build());

        // оновлюємо тільки ті поля що передані (не null)
        if (chest  != null) m.setChest(chest);
        if (waist  != null) m.setWaist(waist);
        if (hips   != null) m.setHips(hips);
        if (height != null) m.setHeight(height);
        if (weight != null) m.setWeight(weight);
        if (preferredFit != null) m.setPreferredFit(preferredFit);

        UserMeasurements saved = measurementsRepository.save(m);
        log.info("SizingService: збережено параметри для sessionId={}", sessionId);
        return toDto(saved);
    }

    // ── getMeasurements ───────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserMeasurementsDto getMeasurements(String sessionId) {
        return measurementsRepository.findBySessionId(sessionId)
                .map(this::toDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasMeasurements(String sessionId) {
        return measurementsRepository.existsBySessionId(sessionId);
    }

    // ── getSizingChart ────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<SizingChartResponse> getSizingChart(String brand, Gender gender) {
        return sizingChartRepository
                .findByBrandAndGenderOrderBySizeAsc(brand, gender)
                .stream()
                .map(this::toChartResponse)
                .toList();
    }

    // ── recommend ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public SizeRecommendation recommend(String sessionId, String brand, Gender gender) {
        // 1. Беремо параметри користувача
        UserMeasurements m = measurementsRepository.findBySessionId(sessionId)
                .orElse(null);

        if (m == null || !m.hasEnoughData()) {
            return new SizeRecommendation(
                    brand, null, null,
                    "Недостатньо даних. Будь ласка, вкажіть параметри тіла: груди, талія або стегна (в см).",
                    null, false, "LOW"
            );
        }

        // 2. Беремо розмірну сітку бренду
        List<SizingChart> charts = sizingChartRepository
                .findByBrandAndGenderOrderBySizeAsc(brand, gender);

        if (charts.isEmpty()) {
            return new SizeRecommendation(
                    brand, null, null,
                    "Розмірна сітка для бренду " + brand + " не знайдена в системі.",
                    null, false, "LOW"
            );
        }

        // 3. Знаходимо найкращий розмір
        return findBestSize(m, charts, brand);
    }

    // ── getAvailableBrands ────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableBrands() {
        return sizingChartRepository.findAllBrands();
    }

    // ── Логіка підбору розміру ────────────────────────────────────

    private SizeRecommendation findBestSize(UserMeasurements m,
                                            List<SizingChart> charts,
                                            String brand) {
        List<SizingChart> matched = new ArrayList<>();

        for (SizingChart chart : charts) {
            int score = 0;
            int total = 0;

            if (m.getChest() != null && chart.getChestMin() != null) {
                total++;
                if (m.getChest() >= chart.getChestMin() && m.getChest() <= chart.getChestMax()) score++;
            }
            if (m.getWaist() != null && chart.getWaistMin() != null) {
                total++;
                if (m.getWaist() >= chart.getWaistMin() && m.getWaist() <= chart.getWaistMax()) score++;
            }
            if (m.getHips() != null && chart.getHipMin() != null) {
                total++;
                if (m.getHips() >= chart.getHipMin() && m.getHips() <= chart.getHipMax()) score++;
            }

            // якщо всі доступні параметри збігаються — це наш розмір
            if (total > 0 && score == total) {
                matched.add(chart);
            }
        }

        // знайдено точний збіг
        if (!matched.isEmpty()) {
            SizingChart best = matched.get(0);
            SizingChart alternative = findAlternative(m, best, charts);
            boolean onBorder = isOnBorder(m, best);

            return new SizeRecommendation(
                    brand,
                    best.getSize(),
                    alternative != null ? alternative.getSize() : null,
                    buildExplanation(m, best, onBorder, m.getPreferredFit()),
                    best.getFitNotes(),
                    onBorder,
                    onBorder ? "MEDIUM" : "HIGH"
            );
        }

        // точного збігу немає — шукаємо найближчий по грудях або талії
        SizingChart closest = findClosest(m, charts);
        if (closest != null) {
            return new SizeRecommendation(
                    brand,
                    closest.getSize(),
                    null,
                    "Точного збігу не знайдено. Найближчий розмір: " + closest.getSize() +
                            ". Рекомендуємо приміряти " + closest.getSize() +
                            " і розмір " + getNextSize(closest.getSize(), charts) + ".",
                    closest.getFitNotes(),
                    true,
                    "LOW"
            );
        }

        return new SizeRecommendation(
                brand, null, null,
                "Не вдалося підібрати розмір. Спробуйте уточнити параметри.",
                null, false, "LOW"
        );
    }

    // чи на межі двох розмірів (параметр в останніх 2см діапазону)
    private boolean isOnBorder(UserMeasurements m, SizingChart chart) {
        int borderZone = 2;
        if (m.getChest() != null && chart.getChestMax() != null) {
            if (chart.getChestMax() - m.getChest() <= borderZone) return true;
        }
        if (m.getWaist() != null && chart.getWaistMax() != null) {
            if (chart.getWaistMax() - m.getWaist() <= borderZone) return true;
        }
        if (m.getHips() != null && chart.getHipMax() != null) {
            if (chart.getHipMax() - m.getHips() <= borderZone) return true;
        }
        return false;
    }

    // знайти наступний розмір як альтернативу
    private SizingChart findAlternative(UserMeasurements m, SizingChart current,
                                        List<SizingChart> charts) {
        if (!isOnBorder(m, current)) return null;
        int idx = charts.indexOf(current);
        return (idx < charts.size() - 1) ? charts.get(idx + 1) : null;
    }

    // найближчий розмір якщо немає точного збігу
    private SizingChart findClosest(UserMeasurements m, List<SizingChart> charts) {
        SizingChart closest = null;
        int minDiff = Integer.MAX_VALUE;

        for (SizingChart chart : charts) {
            int diff = 0;
            if (m.getChest() != null && chart.getChestMin() != null) {
                if (m.getChest() < chart.getChestMin()) diff += chart.getChestMin() - m.getChest();
                else if (m.getChest() > chart.getChestMax()) diff += m.getChest() - chart.getChestMax();
            }
            if (diff < minDiff) { minDiff = diff; closest = chart; }
        }
        return closest;
    }

    private String getNextSize(Size current, List<SizingChart> charts) {
        for (int i = 0; i < charts.size() - 1; i++) {
            if (charts.get(i).getSize() == current) return charts.get(i + 1).getSize().name();
        }
        return current.name();
    }

    // будуємо пояснення з цифрами для LLM
    private String buildExplanation(UserMeasurements m, SizingChart chart,
                                    boolean onBorder, FitType fit) {
        StringBuilder sb = new StringBuilder();
        sb.append("Рекомендований розмір: ").append(chart.getSize()).append("\n");

        if (m.getChest() != null && chart.getChestMin() != null) {
            sb.append("• Груди: ваші ").append(m.getChest()).append("см")
                    .append(" — сітка ").append(chart.getChestMin()).append("-")
                    .append(chart.getChestMax()).append("см ✅\n");
        }
        if (m.getWaist() != null && chart.getWaistMin() != null) {
            sb.append("• Талія: ваші ").append(m.getWaist()).append("см")
                    .append(" — сітка ").append(chart.getWaistMin()).append("-")
                    .append(chart.getWaistMax()).append("см ✅\n");
        }
        if (m.getHips() != null && chart.getHipMin() != null) {
            sb.append("• Стегна: ваші ").append(m.getHips()).append("см")
                    .append(" — сітка ").append(chart.getHipMin()).append("-")
                    .append(chart.getHipMax()).append("см ✅\n");
        }

        if (onBorder) {
            sb.append("\n⚠️ Ви на межі розмірів. ");
            if (fit == FitType.SLIM) sb.append("Оскільки ви надаєте перевагу приталеному крою — залишайтесь на меншому розмірі.");
            else sb.append("Рекомендуємо взяти наступний розмір для комфорту.");
        }

        return sb.toString();
    }

    // ── Mappers ───────────────────────────────────────────────────

    private UserMeasurementsDto toDto(UserMeasurements m) {
        return new UserMeasurementsDto(
                m.getSessionId(),
                m.getChest(), m.getWaist(), m.getHips(),
                m.getHeight(), m.getWeight(),
                m.getPreferredFit(),
                m.hasEnoughData()
        );
    }

    private SizingChartResponse toChartResponse(SizingChart c) {
        return new SizingChartResponse(
                c.getBrand(), c.getSize(), c.getGender(),
                c.getChestMin(), c.getChestMax(),
                c.getWaistMin(), c.getWaistMax(),
                c.getHipMin(), c.getHipMax(),
                c.getHeightFrom(), c.getHeightTo(),
                c.getFitNotes()
        );
    }
}
