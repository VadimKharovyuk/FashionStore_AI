package com.example.fashionstore_ai.repository;

import com.example.fashionstore_ai.model.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;


import com.example.fashionstore_ai.enums.Size;
import com.example.fashionstore_ai.model.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSizeRepository extends JpaRepository<ProductSize, Long> {

    List<ProductSize> findByProductId(Long productId);

    // ShoppingAssistant: перевірити наявність конкретного розміру
    Optional<ProductSize> findByProductIdAndSize(Long productId, Size size);

    // наявні розміри (stockQuantity > 0)
    List<ProductSize> findByProductIdAndStockQuantityGreaterThan(Long productId, int minQty);
}
