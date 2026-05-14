package com.example.fashionstore_ai.dto.userMeasurement;


import com.example.fashionstore_ai.enums.Gender;
import com.example.fashionstore_ai.enums.Size;

public record SizingChartResponse(
        String brand,
        Size size,
        Gender gender,
        Integer chestMin,
        Integer chestMax,
        Integer waistMin,
        Integer waistMax,
        Integer hipMin,
        Integer hipMax,
        Integer heightFrom,
        Integer heightTo,
        String fitNotes      // "Zara маломірить — беріть на розмір більше"
) {}