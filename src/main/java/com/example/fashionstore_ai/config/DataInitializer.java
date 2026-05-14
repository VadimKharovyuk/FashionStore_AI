package com.example.fashionstore_ai.config;

import com.example.fashionstore_ai.enums.*;
import com.example.fashionstore_ai.model.Product;
import com.example.fashionstore_ai.model.ProductSize;
import com.example.fashionstore_ai.repository.ProductRepository;
import com.example.fashionstore_ai.repository.ProductSizeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (productRepository.count() > 0) {
            log.info("DataInitializer: дані вже є, пропускаємо");
            return;
        }

        log.info("DataInitializer: додаємо тестові товари...");
        initProducts();
        log.info("DataInitializer: додано {} товарів", productRepository.count());
    }

    private void initProducts() {

        // ── ВЕЧІРНІ СУКНІ ────────────────────────────────────
        saveProduct(Product.builder()
                        .name("Сукня вечірня з відкритою спиною")
                        .description("Елегантна вечірня сукня міді з шифону. Приталений крій, відкрита спина з V-подібним вирізом. Ідеально для весіль і святкових заходів.")
                        .brand("Zara")
                        .sku("ZR-EVE-001")
                        .price(BigDecimal.valueOf(89.99))
                        .discountPercent(0)
                        .category(Category.EVENING)
                        .gender(Gender.WOMEN)
                        .season(Season.ALL_SEASON)
                        .color(Color.BLACK)
                        .colorDescription("Класичний чорний")
                        .material(Material.POLYESTER)
                        .fabricComposition("95% polyester, 5% elastane")
                        .fitType(FitType.SLIM)
                        .careInstructions("Ручне прання 30°C, не відбілювати, не сушити в барабані")
                        .countryOfOrigin("Португалія")
                        .isNew(true)
                        .isBestseller(false)
                        .styleNotes("Ідеально для весільних заходів та корпоративів")
                        .imageUrl("https://images.unsplash.com/photo-1566479179817-0b9bfb8c4b6c?w=400")
                        .tags(List.of("evening", "wedding", "elegant", "backless"))
                        .build(),
                List.of(Size.XS, Size.S, Size.M, Size.L),
                List.of(3, 8, 12, 5)
        );

        saveProduct(Product.builder()
                        .name("Сукня квіткова міді")
                        .description("Романтична сукня міді з квітковим принтом. Відрізна по талії, спідниця кльош. Підходить для прогулянок і побачень.")
                        .brand("Mango")
                        .sku("MG-FLR-001")
                        .price(BigDecimal.valueOf(69.99))
                        .discountPercent(15)
                        .category(Category.CASUAL)
                        .gender(Gender.WOMEN)
                        .season(Season.SPRING_SUMMER)
                        .color(Color.PRINT)
                        .colorDescription("Квітковий принт на білому фоні")
                        .material(Material.VISCOSE)
                        .fabricComposition("100% viscose")
                        .fitType(FitType.REGULAR)
                        .careInstructions("Делікатне прання 30°C, прасувати при низькій температурі")
                        .countryOfOrigin("Іспанія")
                        .isNew(false)
                        .isBestseller(true)
                        .styleNotes("Не мнеться в дорозі, підходить для подорожей")
                        .imageUrl("https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=400")
                        .tags(List.of("floral", "midi", "romantic", "travel", "summer"))
                        .build(),
                List.of(Size.XS, Size.S, Size.M, Size.L, Size.XL),
                List.of(2, 6, 10, 7, 3)
        );

        saveProduct(Product.builder()
                        .name("Сукня-комбінація атласна")
                        .description("Мінімалістична атласна сукня в стилі slip dress. Тонкі бретелі, пряний крій. Можна носити як самостійно так і з блейзером.")
                        .brand("H&M")
                        .sku("HM-SLI-001")
                        .price(BigDecimal.valueOf(49.99))
                        .discountPercent(0)
                        .category(Category.EVENING)
                        .gender(Gender.WOMEN)
                        .season(Season.ALL_SEASON)
                        .color(Color.BEIGE)
                        .colorDescription("Пудровий бежевий")
                        .material(Material.POLYESTER)
                        .fabricComposition("100% polyester (satin weave)")
                        .fitType(FitType.SLIM)
                        .careInstructions("Ручне прання або делікатний режим, не викручувати")
                        .countryOfOrigin("Бангладеш")
                        .isNew(true)
                        .isBestseller(false)
                        .styleNotes("Трендовий slip dress — носи з кросівками або підборами")
                        .imageUrl("https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=400")
                        .tags(List.of("satin", "slip-dress", "minimalist", "versatile"))
                        .build(),
                List.of(Size.XS, Size.S, Size.M, Size.L),
                List.of(4, 9, 11, 6)
        );

        // ── ПОВСЯКДЕННИЙ ОДЯГ ────────────────────────────────
        saveProduct(Product.builder()
                        .name("Джинси прямого крою")
                        .description("Класичні прямі джинси з високою талією. Щільний деним, не розтягується. Підходять для офісу і прогулянок.")
                        .brand("Zara")
                        .sku("ZR-JNS-001")
                        .price(BigDecimal.valueOf(59.99))
                        .discountPercent(0)
                        .category(Category.CASUAL)
                        .gender(Gender.WOMEN)
                        .season(Season.ALL_SEASON)
                        .color(Color.BLUE)
                        .colorDescription("Класичний синій деним")
                        .material(Material.DENIM)
                        .fabricComposition("98% cotton, 2% elastane")
                        .fitType(FitType.REGULAR)
                        .careInstructions("Прання 30°C навиворіт, не відбілювати")
                        .countryOfOrigin("Туреччина")
                        .isNew(false)
                        .isBestseller(true)
                        .styleNotes("Zara маломірить — рекомендуємо брати на розмір більше")
                        .imageUrl("https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=400")
                        .tags(List.of("jeans", "denim", "casual", "office", "classic"))
                        .build(),
                List.of(Size.XS, Size.S, Size.M, Size.L, Size.XL),
                List.of(5, 12, 15, 8, 4)
        );

        saveProduct(Product.builder()
                        .name("Оверсайз светр з косами")
                        .description("Об'ємний светр з класичним візерунком коси. М'яка суміш вовни та акрилу, приємна до тіла. Ідеально для холодних вечорів.")
                        .brand("Reserved")
                        .sku("RS-SWT-001")
                        .price(BigDecimal.valueOf(45.99))
                        .discountPercent(20)
                        .category(Category.CASUAL)
                        .gender(Gender.WOMEN)
                        .season(Season.FALL_WINTER)
                        .color(Color.BEIGE)
                        .colorDescription("Молочний кремовий")
                        .material(Material.WOOL)
                        .fabricComposition("60% acrylic, 30% wool, 10% polyamide")
                        .fitType(FitType.OVERSIZE)
                        .careInstructions("Ручне прання 20°C, сушити горизонтально")
                        .countryOfOrigin("Польща")
                        .isNew(false)
                        .isBestseller(true)
                        .styleNotes("Оверсайз крій — можна брати свій розмір")
                        .imageUrl("https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=400")
                        .tags(List.of("sweater", "cozy", "winter", "oversized", "knit"))
                        .build(),
                List.of(Size.XS, Size.S, Size.M, Size.L, Size.XL),
                List.of(3, 7, 10, 8, 5)
        );

        saveProduct(Product.builder()
                        .name("Блейзер класичний однобортний")
                        .description("Структурований однобортний блейзер з підкладкою. Підходить для офісу і ділових зустрічей. Не мнеться.")
                        .brand("Massimo Dutti")
                        .sku("MD-BLZ-001")
                        .price(BigDecimal.valueOf(129.99))
                        .discountPercent(0)
                        .category(Category.BUSINESS)
                        .gender(Gender.WOMEN)
                        .season(Season.ALL_SEASON)
                        .color(Color.NAVY)
                        .colorDescription("Темно-синій")
                        .material(Material.POLYESTER)
                        .fabricComposition("70% polyester, 25% viscose, 5% elastane")
                        .fitType(FitType.SLIM)
                        .careInstructions("Хімчистка або делікатне прання 30°C")
                        .countryOfOrigin("Іспанія")
                        .isNew(false)
                        .isBestseller(false)
                        .styleNotes("Масімо Дутті відповідає розмірній сітці — беріть свій розмір")
                        .imageUrl("https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=400")
                        .tags(List.of("blazer", "office", "business", "classic", "structured"))
                        .build(),
                List.of(Size.XS, Size.S, Size.M, Size.L),
                List.of(2, 5, 7, 4)
        );

        // ── СПОРТИВНИЙ ОДЯГ ──────────────────────────────────
        saveProduct(Product.builder()
                        .name("Легінси для йоги Push-Up")
                        .description("Компресійні легінси з ефектом push-up. Матеріал з технологією Dry-Fit — відводить вологу. Широкий пояс не врізається.")
                        .brand("H&M")
                        .sku("HM-SPT-001")
                        .price(BigDecimal.valueOf(34.99))
                        .discountPercent(0)
                        .category(Category.SPORT)
                        .gender(Gender.WOMEN)
                        .season(Season.ALL_SEASON)
                        .color(Color.BLACK)
                        .colorDescription("Чорний з сірими вставками")
                        .material(Material.SYNTHETIC_MIX)
                        .fabricComposition("80% polyamide, 20% elastane")
                        .fitType(FitType.SLIM)
                        .careInstructions("Прання 30°C, не використовувати кондиціонер")
                        .countryOfOrigin("Камбоджа")
                        .isNew(false)
                        .isBestseller(true)
                        .styleNotes("H&M Sport — відповідає розміру, тягнеться добре")
                        .imageUrl("https://images.unsplash.com/photo-1506629082955-511b1aa562c8?w=400")
                        .tags(List.of("sport", "yoga", "leggings", "gym", "activewear"))
                        .build(),
                List.of(Size.XS, Size.S, Size.M, Size.L, Size.XL),
                List.of(8, 15, 20, 12, 6)
        );

        saveProduct(Product.builder()
                        .name("Спортивний топ з підтримкою")
                        .description("Спортивний бра-топ з середньою підтримкою. Знімні чашечки, регульовані бретелі. Підходить для йоги та пілатесу.")
                        .brand("Zara")
                        .sku("ZR-TOP-001")
                        .price(BigDecimal.valueOf(24.99))
                        .discountPercent(10)
                        .category(Category.SPORT)
                        .gender(Gender.WOMEN)
                        .season(Season.ALL_SEASON)
                        .color(Color.OLIVE)
                        .colorDescription("Хакі оливковий")
                        .material(Material.SYNTHETIC_MIX)
                        .fabricComposition("85% polyamide, 15% elastane")
                        .fitType(FitType.SLIM)
                        .careInstructions("Ручне прання або делікатний режим 30°C")
                        .countryOfOrigin("В'єтнам")
                        .isNew(true)
                        .isBestseller(false)
                        .styleNotes("Zara Sport — беріть свій розмір")
                        .imageUrl("https://images.unsplash.com/photo-1571945153237-4929e783af4a?w=400")
                        .tags(List.of("sport", "top", "yoga", "pilates", "activewear"))
                        .build(),
                List.of(Size.XS, Size.S, Size.M, Size.L),
                List.of(6, 10, 14, 8)
        );

        // ── ВЕРХНІЙ ОДЯГ ─────────────────────────────────────
        saveProduct(Product.builder()
                        .name("Пальто оверсайз вовняне")
                        .description("Класичне пальто оверсайз з вовняної суміші. Прямий крій, потайна застібка. Не продувається вітром.")
                        .brand("Massimo Dutti")
                        .sku("MD-COT-001")
                        .price(BigDecimal.valueOf(199.99))
                        .discountPercent(0)
                        .category(Category.CLASSIC)
                        .gender(Gender.WOMEN)
                        .season(Season.FALL_WINTER)
                        .color(Color.BEIGE)
                        .colorDescription("Карамельний кемел")
                        .material(Material.WOOL)
                        .fabricComposition("60% wool, 30% polyester, 10% other")
                        .fitType(FitType.OVERSIZE)
                        .careInstructions("Тільки хімчистка")
                        .countryOfOrigin("Іспанія")
                        .isNew(false)
                        .isBestseller(true)
                        .styleNotes("Оверсайз крій — беріть свій розмір або на розмір менше для більш приталеного силуету")
                        .imageUrl("https://images.unsplash.com/photo-1539533018447-63fcce2678e3?w=400")
                        .tags(List.of("coat", "wool", "winter", "oversize", "classic", "camel"))
                        .build(),
                List.of(Size.XS, Size.S, Size.M, Size.L, Size.XL),
                List.of(2, 4, 6, 5, 3)
        );

        saveProduct(Product.builder()
                        .name("Джинсова куртка оверсайз")
                        .description("Класична джинсова куртка вільного крою. Потерта обробка, металеві кнопки. Вічна класика гардеробу.")
                        .brand("H&M")
                        .sku("HM-JKT-001")
                        .price(BigDecimal.valueOf(54.99))
                        .discountPercent(0)
                        .category(Category.CASUAL)
                        .gender(Gender.UNISEX)
                        .season(Season.SPRING_SUMMER)
                        .color(Color.BLUE)
                        .colorDescription("Середньо-синій деним з потертостями")
                        .material(Material.DENIM)
                        .fabricComposition("100% cotton")
                        .fitType(FitType.OVERSIZE)
                        .careInstructions("Прання 30°C навиворіт")
                        .countryOfOrigin("Бангладеш")
                        .isNew(false)
                        .isBestseller(false)
                        .styleNotes("Унісекс — жінки беруть на 1-2 розміри менше від свого")
                        .imageUrl("https://images.unsplash.com/photo-1601333144130-8cbb312386b6?w=400")
                        .tags(List.of("denim", "jacket", "unisex", "casual", "spring"))
                        .build(),
                List.of(Size.XS, Size.S, Size.M, Size.L, Size.XL, Size.XXL),
                List.of(4, 8, 12, 10, 6, 3)
        );

        // ── ПЛЯЖНИЙ ОДЯГ ─────────────────────────────────────
        saveProduct(Product.builder()
                        .name("Сарафан пляжний з мушлями")
                        .description("Легкий пляжний сарафан з вишивкою у вигляді мушель. Тканина швидко сохне. Ідеально для пляжу і прогулянок по набережній.")
                        .brand("Mango")
                        .sku("MG-BCH-001")
                        .price(BigDecimal.valueOf(39.99))
                        .discountPercent(25)
                        .category(Category.BEACH)
                        .gender(Gender.WOMEN)
                        .season(Season.SUMMER)
                        .color(Color.WHITE)
                        .colorDescription("Білий з блакитною вишивкою")
                        .material(Material.COTTON)
                        .fabricComposition("100% cotton")
                        .fitType(FitType.RELAXED)
                        .careInstructions("Прання 40°C, можна в барабані")
                        .countryOfOrigin("Індія")
                        .isNew(true)
                        .isBestseller(false)
                        .styleNotes("Вільний крій — беріть свій розмір")
                        .imageUrl("https://images.unsplash.com/photo-1469334031218-e382a71b716b?w=400")
                        .tags(List.of("beach", "summer", "boho", "vacation", "resort"))
                        .build(),
                List.of(Size.XS, Size.S, Size.M, Size.L, Size.XL),
                List.of(5, 10, 12, 8, 4)
        );

        // ── ДІЛОВЕ ───────────────────────────────────────────
        saveProduct(Product.builder()
                        .name("Брюки класичні зі стрілками")
                        .description("Класичні офісні брюки зі стрілками. Щільна тканина тримає форму весь день. Підходять до блейзера і сорочки.")
                        .brand("Massimo Dutti")
                        .sku("MD-TRS-001")
                        .price(BigDecimal.valueOf(89.99))
                        .discountPercent(0)
                        .category(Category.BUSINESS)
                        .gender(Gender.WOMEN)
                        .season(Season.ALL_SEASON)
                        .color(Color.BLACK)
                        .colorDescription("Чорний")
                        .material(Material.POLYESTER)
                        .fabricComposition("65% polyester, 30% viscose, 5% elastane")
                        .fitType(FitType.SLIM)
                        .careInstructions("Делікатне прання 30°C або хімчистка, прасувати")
                        .countryOfOrigin("Іспанія")
                        .isNew(false)
                        .isBestseller(false)
                        .styleNotes("Масімо Дутті — відповідає розміру")
                        .imageUrl("https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=400")
                        .tags(List.of("office", "business", "trousers", "classic", "formal"))
                        .build(),
                List.of(Size.XS, Size.S, Size.M, Size.L),
                List.of(3, 6, 8, 5)
        );

        // ── STREETWEAR ────────────────────────────────────────
        saveProduct(Product.builder()
                        .name("Худі оверсайз з принтом")
                        .description("Об'ємне худі з графічним принтом на спині. М'яка флісова підкладка. Підходить для прогулянок і casual виходів.")
                        .brand("H&M")
                        .sku("HM-HOD-001")
                        .price(BigDecimal.valueOf(34.99))
                        .discountPercent(0)
                        .category(Category.STREETWEAR)
                        .gender(Gender.UNISEX)
                        .season(Season.FALL_WINTER)
                        .color(Color.GREY)
                        .colorDescription("Сірий меланж")
                        .material(Material.COTTON)
                        .fabricComposition("80% cotton, 20% polyester")
                        .fitType(FitType.OVERSIZE)
                        .careInstructions("Прання 40°C навиворіт, не відбілювати")
                        .countryOfOrigin("Бангладеш")
                        .isNew(true)
                        .isBestseller(false)
                        .styleNotes("Унісекс оверсайз — жінки беруть XS-S для вільного крою")
                        .imageUrl("https://images.unsplash.com/photo-1556821840-3a63f15732ce?w=400")
                        .tags(List.of("hoodie", "streetwear", "unisex", "casual", "oversized"))
                        .build(),
                List.of(Size.XS, Size.S, Size.M, Size.L, Size.XL, Size.XXL),
                List.of(6, 12, 18, 14, 8, 4)
        );

        saveProduct(Product.builder()
                        .name("Карго штани з кишенями")
                        .description("Трендові карго штани з великими бічними кишенями. Тканина не мнеться. Еластичний пояс для комфорту.")
                        .brand("Zara")
                        .sku("ZR-CRG-001")
                        .price(BigDecimal.valueOf(49.99))
                        .discountPercent(0)
                        .category(Category.STREETWEAR)
                        .gender(Gender.WOMEN)
                        .season(Season.ALL_SEASON)
                        .color(Color.OLIVE)
                        .colorDescription("Оливковий хакі")
                        .material(Material.COTTON)
                        .fabricComposition("100% cotton")
                        .fitType(FitType.RELAXED)
                        .careInstructions("Прання 30°C, прасувати при середній температурі")
                        .countryOfOrigin("Туреччина")
                        .isNew(true)
                        .isBestseller(true)
                        .styleNotes("Zara — беріть свій розмір або на розмір більше")
                        .imageUrl("https://images.unsplash.com/photo-1594938291221-94f18cbb5660?w=400")
                        .tags(List.of("cargo", "streetwear", "trendy", "utility", "casual"))
                        .build(),
                List.of(Size.XS, Size.S, Size.M, Size.L, Size.XL),
                List.of(4, 9, 13, 9, 5)
        );

        // ── LOUNGEWEAR ────────────────────────────────────────
        saveProduct(Product.builder()
                        .name("Піжамний комплект сатиновий")
                        .description("Розкішний піжамний комплект з сатину: сорочка і штани. Приємний до тіла матеріал. Можна носити як loungewear вдома.")
                        .brand("H&M")
                        .sku("HM-PJM-001")
                        .price(BigDecimal.valueOf(44.99))
                        .discountPercent(0)
                        .category(Category.LOUNGEWEAR)
                        .gender(Gender.WOMEN)
                        .season(Season.ALL_SEASON)
                        .color(Color.PINK)
                        .colorDescription("Пудровий рожевий")
                        .material(Material.POLYESTER)
                        .fabricComposition("100% polyester (satin)")
                        .fitType(FitType.RELAXED)
                        .careInstructions("Делікатне прання 30°C, не крутити")
                        .countryOfOrigin("Китай")
                        .isNew(false)
                        .isBestseller(true)
                        .styleNotes("Вільний крій — беріть свій розмір")
                        .imageUrl("https://images.unsplash.com/photo-1617952186950-c7a29b0f6b49?w=400")
                        .tags(List.of("pajama", "lounge", "satin", "home", "gift"))
                        .build(),
                List.of(Size.XS, Size.S, Size.M, Size.L, Size.XL),
                List.of(4, 8, 11, 7, 3)
        );
    }

    private void saveProduct(Product product,
                             List<Size> sizes,
                             List<Integer> quantities) {
        Product saved = productRepository.save(product);

        for (int i = 0; i < sizes.size(); i++) {
            ProductSize ps = ProductSize.builder()
                    .product(saved)
                    .size(sizes.get(i))
                    .stockQuantity(quantities.get(i))
                    .build();
            productSizeRepository.save(ps);
        }
    }
}