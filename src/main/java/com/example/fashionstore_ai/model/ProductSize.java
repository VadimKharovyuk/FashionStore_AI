package com.example.fashionstore_ai.model;

import com.example.fashionstore_ai.enums.Size;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_size")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Size size;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

}