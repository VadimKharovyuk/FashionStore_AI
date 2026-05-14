package com.example.fashionstore_ai.repository;

import com.example.fashionstore_ai.enums.Gender;
import com.example.fashionstore_ai.enums.Size;
import com.example.fashionstore_ai.model.SizingChart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SizingChartRepository extends JpaRepository<SizingChart, Long> {
    List<SizingChart> findByBrandAndGender(String brand, Gender gender);
    Optional<SizingChart> findByBrandAndSizeAndGender(String brand, Size size, Gender gender);
}