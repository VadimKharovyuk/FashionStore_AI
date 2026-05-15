package com.example.fashionstore_ai.repository;


import com.example.fashionstore_ai.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // всі повідомлення сесії (для відображення в UI)
    List<ChatMessage> findByChatSessionIdOrderByCreatedAtAsc(Long sessionId);

    // кількість повідомлень в сесії (щоб вирішити чи треба summary)
    long countByChatSessionId(Long sessionId);

    // Рівень 2: pinned повідомлення (якір) — завжди в контексті
    List<ChatMessage> findByChatSessionIdAndIsPinnedTrueOrderByCreatedAtAsc(Long sessionId);

    // Рівень 1+2: останні N повідомлень для sliding window
    // використовується після того як pinned вже додані
    @Query("""
            SELECT m FROM ChatMessage m
            WHERE m.chatSession.id = :sessionId
              AND m.isPinned = false
            ORDER BY m.createdAt DESC
            LIMIT :limit
            """)
    List<ChatMessage> findLastNNonPinned(@Param("sessionId") Long sessionId,
                                         @Param("limit") int limit);

    // Рівень 3: повідомлення які ще не стиснуті в summary
    // messageIndex > summarizedCount
    @Query("""
            SELECT m FROM ChatMessage m
            WHERE m.chatSession.id = :sessionId
              AND m.messageIndex > :fromIndex
            ORDER BY m.createdAt ASC
            """)
    List<ChatMessage> findAfterIndex(@Param("sessionId") Long sessionId,
                                     @Param("fromIndex") int fromIndex);

    // повідомлення для стискання (старі, не pinned, до певного індексу)
    @Query("""
            SELECT m FROM ChatMessage m
            WHERE m.chatSession.id = :sessionId
              AND m.isPinned = false
              AND m.messageIndex <= :toIndex
            ORDER BY m.createdAt ASC
            """)
    List<ChatMessage> findForSummarization(@Param("sessionId") Long sessionId,
                                           @Param("toIndex") int toIndex);


    @Modifying
    @Transactional
    void deleteByChatSessionId(Long chatSessionId);
}
