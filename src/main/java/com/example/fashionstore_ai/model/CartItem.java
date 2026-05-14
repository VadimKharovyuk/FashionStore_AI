package com.example.fashionstore_ai.model;

import com.example.fashionstore_ai.enums.Size;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_item", indexes = {
        @Index(name = "idx_cart_item_cart_id", columnList = "cart_id")
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Size size;

    @Column(nullable = false)
    private Integer quantity;

    // ціна на момент додавання — фіксуємо щоб зміна ціни не впливала на кошик
    @Column(name = "price_at_add", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtAdd;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ── Helper ────────────────────────────────────────────────────

    public BigDecimal getSubtotal() {
        return priceAtAdd.multiply(BigDecimal.valueOf(quantity));
    }
}

