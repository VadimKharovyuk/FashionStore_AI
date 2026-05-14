package com.example.fashionstore_ai.repository;


import com.example.fashionstore_ai.enums.Size;
import com.example.fashionstore_ai.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // перевірити чи вже є такий товар+розмір в кошику
    // щоб інкрементувати quantity а не дублювати
    Optional<CartItem> findByCartIdAndProductIdAndSize(Long cartId, Long productId, Size size);

    List<CartItem> findByCartId(Long cartId);

    // видалити всі items кошика (при оформленні замовлення)
    void deleteByCartId(Long cartId);
}
