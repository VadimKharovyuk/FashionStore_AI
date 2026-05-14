package com.example.fashionstore_ai.repository;

import com.example.fashionstore_ai.enums.Gender;
import com.example.fashionstore_ai.enums.Size;
import com.example.fashionstore_ai.model.SizingChart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


import com.example.fashionstore_ai.enums.Gender;
import com.example.fashionstore_ai.enums.Size;
import com.example.fashionstore_ai.model.SizingChart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SizingChartRepository extends JpaRepository<SizingChart, Long> {

    // SizingAgent: вся сітка бренду для порівняння
    List<SizingChart> findByBrandAndGenderOrderBySizeAsc(String brand, Gender gender);

    // конкретний розмір бренду
    Optional<SizingChart> findByBrandAndSizeAndGender(String brand, Size size, Gender gender);

    // всі бренди що є в системі (для підказки користувачу)
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT s.brand FROM SizingChart s ORDER BY s.brand")
    List<String> findAllBrands();
}