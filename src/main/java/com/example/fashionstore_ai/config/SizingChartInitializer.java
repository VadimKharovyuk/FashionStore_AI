package com.example.fashionstore_ai.config;

import com.example.fashionstore_ai.enums.Gender;
import com.example.fashionstore_ai.enums.Size;
import com.example.fashionstore_ai.model.SizingChart;
import com.example.fashionstore_ai.repository.SizingChartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // DataInitializer має @Order(1) або дефолт
public class SizingChartInitializer implements ApplicationRunner {

    private final SizingChartRepository sizingChartRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (sizingChartRepository.count() > 0) {
            log.info("SizingChartInitializer: дані вже є, пропускаємо");
            return;
        }

        log.info("SizingChartInitializer: додаємо розмірні сітки брендів...");

        initZaraWomen();
        initHMWomen();
        initMangoWomen();
        initMassimoDuttiWomen();
        initReservedWomen();

        initZaraMen();
        initHMMen();
        initMangoMen();
        initMassimoDuttiMen();
        initReservedMen();

        log.info("SizingChartInitializer: додано {} записів", sizingChartRepository.count());
    }

    // ─────────────────────────────────────────────────────────────────
    // ZARA — маломірить, рекомендуємо на розмір більше
    // ─────────────────────────────────────────────────────────────────
    private void initZaraWomen() {
        String brand = "Zara";
        Gender gender = Gender.WOMEN;
        String note = "Zara маломірить — рекомендуємо брати на розмір більше. " +
                "Особливо помітно в джинсах і приталених сукнях.";

        sizingChartRepository.saveAll(List.of(
                chart(brand, Size.XS, gender, 76, 80, 58, 62, 82, 86, 158, 170, note),
                chart(brand, Size.S,  gender, 80, 84, 62, 66, 86, 90, 158, 172, note),
                chart(brand, Size.M,  gender, 84, 88, 66, 70, 90, 94, 160, 174, note),
                chart(brand, Size.L,  gender, 88, 93, 70, 75, 94, 99, 162, 176, note),
                chart(brand, Size.XL, gender, 93, 98, 75, 80, 99, 104, 162, 178, note)
        ));

        log.info("  → Zara WOMEN: {} розмірів", 5);
    }

    // ─────────────────────────────────────────────────────────────────
    // H&M — відповідає стандартній розмірній сітці
    // ─────────────────────────────────────────────────────────────────
    private void initHMWomen() {
        String brand = "H&M";
        Gender gender = Gender.WOMEN;
        String note = "H&M відповідає стандартній європейській розмірній сітці. " +
                "Беріть свій звичайний розмір. Спортивний одяг тягнеться добре.";

        sizingChartRepository.saveAll(List.of(
                chart(brand, Size.XS, gender, 78, 82, 60, 64, 84, 88,  158, 170, note),
                chart(brand, Size.S,  gender, 82, 86, 64, 68, 88, 92,  160, 172, note),
                chart(brand, Size.M,  gender, 86, 90, 68, 72, 92, 96,  162, 174, note),
                chart(brand, Size.L,  gender, 90, 95, 72, 77, 96, 101, 164, 176, note),
                chart(brand, Size.XL, gender, 95, 100, 77, 82, 101, 106, 164, 178, note),
                chart(brand, Size.XXL,  gender, 100, 106, 82,  88, 106, 112, 164, 180, note)
        ));

        log.info("  → H&M WOMEN: {} розмірів", 6);
    }

    // ─────────────────────────────────────────────────────────────────
    // MANGO — трохи більшомірить, можна брати на розмір менше
    // ─────────────────────────────────────────────────────────────────
    private void initMangoWomen() {
        String brand = "Mango";
        Gender gender = Gender.WOMEN;
        String note = "Mango трохи більшомірить порівняно з іншими брендами. " +
                "Якщо ви на межі розмірів — беріть менший. " +
                "Сукні і блузи особливо вільні в плечах.";

        sizingChartRepository.saveAll(List.of(
                chart(brand, Size.XS, gender, 80, 84, 62, 66, 86, 90,  158, 170, note),
                chart(brand, Size.S,  gender, 84, 88, 66, 70, 90, 94,  160, 172, note),
                chart(brand, Size.M,  gender, 88, 92, 70, 74, 94, 98,  162, 174, note),
                chart(brand, Size.L,  gender, 92, 97, 74, 79, 98, 103, 162, 176, note),
                chart(brand, Size.XL, gender, 97, 103, 79, 85, 103, 109, 164, 178, note)
        ));

        log.info("  → Mango WOMEN: {} розмірів", 5);
    }

    // ─────────────────────────────────────────────────────────────────
    // MASSIMO DUTTI — точно відповідає розмірній сітці, преміум крій
    // ─────────────────────────────────────────────────────────────────
    private void initMassimoDuttiWomen() {
        String brand = "Massimo Dutti";
        Gender gender = Gender.WOMEN;
        String note = "Massimo Dutti точно відповідає розмірній сітці — беріть свій розмір. " +
                "Преміум крій з урахуванням фігури. Блейзери і брюки сидять бездоганно.";

        sizingChartRepository.saveAll(List.of(
                chart(brand, Size.XS, gender, 78, 82, 60, 64, 84, 88,  158, 168, note),
                chart(brand, Size.S,  gender, 82, 86, 64, 68, 88, 92,  160, 172, note),
                chart(brand, Size.M,  gender, 86, 90, 68, 72, 92, 96,  162, 174, note),
                chart(brand, Size.L,  gender, 90, 94, 72, 76, 96, 100, 164, 176, note),
                chart(brand, Size.XL, gender, 94, 99, 76, 81, 100, 105, 164, 178, note)
        ));

        log.info("  → Massimo Dutti WOMEN: {} розмірів", 5);
    }

    // ─────────────────────────────────────────────────────────────────
    // RESERVED — відповідає сітці, але светри краще брати на розмір більше
    // ─────────────────────────────────────────────────────────────────
    private void initReservedWomen() {
        String brand = "Reserved";
        Gender gender = Gender.WOMEN;
        String note = "Reserved в цілому відповідає стандартній сітці. " +
                "Трикотаж і светри — рекомендуємо на розмір більше для комфортної посадки. " +
                "Джинси відповідають розміру.";

        sizingChartRepository.saveAll(List.of(
                chart(brand, Size.XS, gender, 78, 82, 60, 64, 84, 88,  158, 170, note),
                chart(brand, Size.S,  gender, 82, 86, 64, 68, 88, 92,  160, 172, note),
                chart(brand, Size.M,  gender, 86, 90, 68, 72, 92, 96,  162, 174, note),
                chart(brand, Size.L,  gender, 90, 95, 72, 77, 96, 101, 162, 176, note),
                chart(brand, Size.XL, gender, 95, 101, 77, 83, 101, 107, 164, 178, note),
                chart(brand, Size.XXL, gender, 101, 107, 83, 89, 107, 113, 164, 180, note)
        ));

        log.info("  → Reserved WOMEN: {} розмірів", 6);
    }

    // ─────────────────────────────────────────────────────────────────
    // ЧОЛОВІЧА СІТКА
    // ─────────────────────────────────────────────────────────────────

    private void initZaraMen() {
        String brand = "Zara";
        Gender gender = Gender.MEN;
        String note = "Zara MAN маломірить — рекомендуємо брати на розмір більше. " +
                "Особливо актуально для сорочок і піджаків у плечах.";

        sizingChartRepository.saveAll(List.of(
                chart(brand, Size.S,    gender, 88,  92,  73,  77,  88,  92,  170, 178, note),
                chart(brand, Size.M,    gender, 92,  96,  77,  81,  92,  96,  174, 182, note),
                chart(brand, Size.L,    gender, 96,  100, 81,  85,  96,  100, 178, 186, note),
                chart(brand, Size.XL,   gender, 100, 105, 85,  90,  100, 105, 180, 188, note),
                chart(brand, Size.XXL,  gender, 105, 111, 90,  96,  105, 111, 182, 190, note)
        ));

        log.info("  → Zara MEN: {} розмірів", 5);
    }

    private void initHMMen() {
        String brand = "H&M";
        Gender gender = Gender.MEN;
        String note = "H&M Men відповідає стандартній європейській сітці. " +
                "Беріть свій звичайний розмір. " +
                "Спортивний одяг — можна брати на розмір менше для щільної посадки.";

        sizingChartRepository.saveAll(List.of(
                chart(brand, Size.S,    gender, 88,  92,  74,  78,  88,  92,  170, 178, note),
                chart(brand, Size.M,    gender, 92,  96,  78,  82,  92,  96,  174, 182, note),
                chart(brand, Size.L,    gender, 96,  101, 82,  87,  96,  101, 178, 186, note),
                chart(brand, Size.XL,   gender, 101, 107, 87,  93,  101, 107, 180, 188, note),
                chart(brand, Size.XXL,  gender, 107, 113, 93,  99,  107, 113, 182, 190, note),
                chart(brand, Size.XXL,  gender, 107, 113, 93,  99,  107, 113, 182, 190, note)
        ));

        log.info("  → H&M MEN: {} розмірів", 5);
    }

    private void initMangoMen() {
        String brand = "Mango";
        Gender gender = Gender.MEN;
        String note = "Mango Man відповідає стандартній сітці, крій приталений. " +
                "Якщо широкі плечі або груди — беріть на розмір більше. " +
                "Сорочки slim fit — вузькі в плечах.";

        sizingChartRepository.saveAll(List.of(
                chart(brand, Size.S,   gender, 87,  91,  73,  77,  87,  91,  170, 178, note),
                chart(brand, Size.M,   gender, 91,  95,  77,  81,  91,  95,  174, 182, note),
                chart(brand, Size.L,   gender, 95,  99,  81,  85,  95,  99,  176, 184, note),
                chart(brand, Size.XL,  gender, 99,  104, 85,  90,  99,  104, 178, 186, note),
                chart(brand, Size.XXL, gender, 104, 110, 90,  96,  104, 110, 180, 188, note)
        ));

        log.info("  → Mango MEN: {} розмірів", 5);
    }

    private void initMassimoDuttiMen() {
        String brand = "Massimo Dutti";
        Gender gender = Gender.MEN;
        String note = "Massimo Dutti Men — точна преміум сітка, беріть свій розмір. " +
                "Піджаки і сорочки сидять бездоганно за умови правильного розміру. " +
                "Брюки — стандартна посадка, не потребують підгонки.";

        sizingChartRepository.saveAll(List.of(
                chart(brand, Size.S,   gender, 88,  92,  74,  78,  88,  92,  170, 178, note),
                chart(brand, Size.M,   gender, 92,  96,  78,  82,  92,  96,  174, 182, note),
                chart(brand, Size.L,   gender, 96,  100, 82,  86,  96,  100, 178, 186, note),
                chart(brand, Size.XL,  gender, 100, 105, 86,  91,  100, 105, 180, 188, note),
                chart(brand, Size.XXL, gender, 105, 111, 91,  97,  105, 111, 182, 190, note)
        ));

        log.info("  → Massimo Dutti MEN: {} розмірів", 5);
    }

    private void initReservedMen() {
        String brand = "Reserved";
        Gender gender = Gender.MEN;
        String note = "Reserved Men в цілому відповідає стандартній сітці. " +
                "Трикотаж і светри — беріть на розмір більше. " +
                "Джинси regular fit — відповідають розміру.";

        sizingChartRepository.saveAll(List.of(
                chart(brand, Size.S,    gender, 88,  92,  74,  78,  88,  92,  170, 178, note),
                chart(brand, Size.M,    gender, 92,  96,  78,  82,  92,  96,  174, 182, note),
                chart(brand, Size.L,    gender, 96,  101, 82,  87,  96,  101, 178, 186, note),
                chart(brand, Size.XL,   gender, 101, 107, 87,  93,  101, 107, 180, 188, note),
                chart(brand, Size.XXL,  gender, 107, 113, 93,  99,  107, 113, 182, 190, note),
                chart(brand, Size.XXL, gender, 107, 113, 93,  99,  107, 113, 182, 190, note)
        ));

        log.info("  → Reserved MEN: {} розмірів", 5);
    }

    // ─────────────────────────────────────────────────────────────────
    // Builder helper — щоб не дублювати new SizingChart() скрізь
    // ─────────────────────────────────────────────────────────────────
    private SizingChart chart(String brand, Size size, Gender gender,
                              int chestMin, int chestMax,
                              int waistMin, int waistMax,
                              int hipMin, int hipMax,
                              int heightFrom, int heightTo,
                              String fitNotes) {
        return SizingChart.builder()
                .brand(brand)
                .size(size)
                .gender(gender)
                .chestMin(chestMin)
                .chestMax(chestMax)
                .waistMin(waistMin)
                .waistMax(waistMax)
                .hipMin(hipMin)
                .hipMax(hipMax)
                .heightFrom(heightFrom)
                .heightTo(heightTo)
                .fitNotes(fitNotes)
                .build();
    }
}