package com.example.fashionstore_ai.model;

import com.example.fashionstore_ai.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String brand;

    @Column(unique = true, length = 100)
    private String sku;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_percent")
    private Integer discountPercent = 0;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Season season;

    @Enumerated(EnumType.STRING)
    private Color color;

    @Column(name = "color_description", length = 200)
    private String colorDescription;

    @Enumerated(EnumType.STRING)
    private Material material;

    @Column(name = "fabric_composition", length = 300)
    private String fabricComposition;

    @Enumerated(EnumType.STRING)
    @Column(name = "fit_type")
    private FitType fitType;

    @Column(name = "care_instructions", columnDefinition = "TEXT")
    private String careInstructions;

    @Column(name = "country_of_origin", length = 100)
    private String countryOfOrigin;

    @Column(name = "is_new")
    private Boolean isNew = false;

    @Column(name = "is_bestseller")
    private Boolean isBestseller = false;

    @Column(name = "style_notes", columnDefinition = "TEXT")
    private String styleNotes;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // Зв'язок з розмірами і наявністю
    @OneToMany(mappedBy = "product",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<ProductSize> sizes = new ArrayList<>();

    // Теги для RecommendationAgent
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "product_tags",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    // Додаткові фото
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "product_images",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "image_url")
    private List<String> additionalImages = new ArrayList<>();



    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
