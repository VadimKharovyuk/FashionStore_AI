package com.example.fashionstore_ai.model;

import com.example.fashionstore_ai.enums.Gender;
import com.example.fashionstore_ai.enums.Size;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


//Потрібна для SizingAgent — коли користувач питає:
//        "який розмір мені підійде в Zara,
//груди 90, талія 70, стегна 96?"
//Агент:
//
//Бере параметри користувача
//Шукає розмірну сітку Zara через getSizingChart(brand)
//Порівнює і відповідає з поясненням


@Entity
@Table(name = "sizing_chart")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SizingChart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String brand;               // "Zara", "H&M", "Mango"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Size size;                  // S, M, L ...

    @Enumerated(EnumType.STRING)
    private Gender gender;              // WOMEN / MEN / UNISEX

    // Груди (см)
    @Column(name = "chest_min")
    private Integer chestMin;

    @Column(name = "chest_max")
    private Integer chestMax;

    // Талія (см)
    @Column(name = "waist_min")
    private Integer waistMin;

    @Column(name = "waist_max")
    private Integer waistMax;

    // Стегна (см)
    @Column(name = "hip_min")
    private Integer hipMin;

    @Column(name = "hip_max")
    private Integer hipMax;

    // Зріст (см)
    @Column(name = "height_from")
    private Integer heightFrom;

    @Column(name = "height_to")
    private Integer heightTo;

    // Нотатки для агента
    @Column(name = "fit_notes", columnDefinition = "TEXT")
    private String fitNotes;            // "Zara маломірить — беріть на розмір більше"

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}


