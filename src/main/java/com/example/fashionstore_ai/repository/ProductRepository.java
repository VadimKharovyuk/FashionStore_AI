package com.example.fashionstore_ai.repository;

import com.example.fashionstore_ai.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,Long> {
}
