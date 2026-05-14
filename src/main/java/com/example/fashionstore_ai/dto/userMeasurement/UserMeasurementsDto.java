package com.example.fashionstore_ai.dto.userMeasurement;


import com.example.fashionstore_ai.enums.FitType;

public record UserMeasurementsDto(
        String sessionId,
        Integer chest,
        Integer waist,
        Integer hips,
        Integer height,
        Integer weight,
        FitType preferredFit,
        boolean hasEnoughData  // chest || waist || hips != null
) {}

