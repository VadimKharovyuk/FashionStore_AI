package com.example.fashionstore_ai.repository;


import com.example.fashionstore_ai.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    // SupportAgent: знайти конкретний item для повернення
    // перевіряємо що item належить саме цьому замовленню
    Optional<OrderItem> findByIdAndOrderId(Long itemId, Long orderId);

    // всі items з запитом на повернення — для адмін панелі
    List<OrderItem> findByReturnStatusNotNull();
}
