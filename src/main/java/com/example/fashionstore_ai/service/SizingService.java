package com.example.fashionstore_ai.service;

import com.example.fashionstore_ai.dto.userMeasurement.SizeRecommendation;
import com.example.fashionstore_ai.dto.userMeasurement.SizingChartResponse;
import com.example.fashionstore_ai.dto.userMeasurement.UserMeasurementsDto;
import com.example.fashionstore_ai.enums.FitType;
import com.example.fashionstore_ai.enums.Gender;

import java.util.List;

public interface SizingService {

    // зберегти або оновити параметри тіла користувача
    UserMeasurementsDto saveMeasurements(String sessionId,
                                         Integer chest,
                                         Integer waist,
                                         Integer hips,
                                         Integer height,
                                         Integer weight,
                                         FitType preferredFit);

    // отримати збережені параметри
    UserMeasurementsDto getMeasurements(String sessionId);

    // перевірити чи є збережені параметри
    boolean hasMeasurements(String sessionId);

    // отримати розмірну сітку бренду
    List<SizingChartResponse> getSizingChart(String brand, Gender gender);

    // головний метод — порівняти параметри з сіткою і дати рекомендацію
    SizeRecommendation recommend(String sessionId, String brand, Gender gender);

    // список брендів що є в системі
    List<String> getAvailableBrands();
}
