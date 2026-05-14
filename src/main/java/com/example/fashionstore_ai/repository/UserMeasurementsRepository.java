package com.example.fashionstore_ai.repository;

import com.example.fashionstore_ai.model.UserMeasurements;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMeasurementsRepository extends JpaRepository<UserMeasurements, Long> {

    // SizingAgent: отримати параметри по сесії
    Optional<UserMeasurements> findBySessionId(String sessionId);

    // перевірити чи вже є параметри (щоб не питати знову)
    boolean existsBySessionId(String sessionId);
}