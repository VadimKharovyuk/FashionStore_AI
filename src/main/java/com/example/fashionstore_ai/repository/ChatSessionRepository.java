package com.example.fashionstore_ai.repository;

import com.example.fashionstore_ai.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    // знайти активну сесію по sessionId
    Optional<ChatSession> findBySessionIdAndStatus(String sessionId, String status);

    // знайти будь-яку останню сесію (для відновлення)
    Optional<ChatSession> findTopBySessionIdOrderByCreatedAtDesc(String sessionId);

    boolean existsBySessionId(String sessionId);
}