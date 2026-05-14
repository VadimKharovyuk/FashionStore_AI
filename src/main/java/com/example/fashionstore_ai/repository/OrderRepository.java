package com.example.fashionstore_ai.repository;
import com.example.fashionstore_ai.enums.OrderStatus;
import com.example.fashionstore_ai.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // SupportAgent: знайти замовлення по номеру і сесії
    // sessionId обов'язковий — щоб не можна було дивитись чужі замовлення
    Optional<Order> findByOrderNumberAndSessionId(String orderNumber, String sessionId);

    // всі замовлення сесії (для відображення історії)
    List<Order> findBySessionIdOrderByCreatedAtDesc(String sessionId);

    // фільтр по статусу — для адмін-панелі або аналітики
    List<Order> findBySessionIdAndStatus(String sessionId, OrderStatus status);

    // з items одразу — уникаємо N+1
    @Query("""
            SELECT o FROM Order o
            LEFT JOIN FETCH o.items i
            LEFT JOIN FETCH i.product
            WHERE o.orderNumber = :orderNumber
              AND o.sessionId = :sessionId
            """)
    Optional<Order> findByOrderNumberAndSessionIdWithItems(
            @Param("orderNumber") String orderNumber,
            @Param("sessionId") String sessionId
    );

    boolean existsByOrderNumber(String orderNumber);
}
