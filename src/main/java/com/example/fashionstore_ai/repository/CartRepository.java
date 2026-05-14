package com.example.fashionstore_ai.repository;


import com.example.fashionstore_ai.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // ShoppingAssistant: знайти кошик по сесії
    Optional<Cart> findBySessionId(String sessionId);

    boolean existsBySessionId(String sessionId);

    // з items одразу — щоб уникнути N+1 при відображенні кошика
    @Query("""
            SELECT c FROM Cart c
            LEFT JOIN FETCH c.items i
            LEFT JOIN FETCH i.product
            WHERE c.sessionId = :sessionId
            """)
    Optional<Cart> findBySessionIdWithItems(@Param("sessionId") String sessionId);
}
