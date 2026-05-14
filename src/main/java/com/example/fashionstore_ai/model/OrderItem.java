package com.example.fashionstore_ai.model;
import com.example.fashionstore_ai.enums.Size;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_item", indexes = {
        @Index(name = "idx_order_item_order_id", columnList = "order_id")
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Size size;

    @Column(nullable = false)
    private Integer quantity;

    // ціна на момент замовлення — незмінна
    @Column(name = "price_at_order", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtOrder;

    // ── Повернення (SupportAgent) ─────────────────────────────────
    @Column(name = "return_status", length = 50)
    private String returnStatus;   // null / REQUESTED / COMPLETED

    @Column(name = "return_reason", columnDefinition = "TEXT")
    private String returnReason;

    @Column(name = "return_requested_at")
    private LocalDateTime returnRequestedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ── Helper ────────────────────────────────────────────────────

    public BigDecimal getSubtotal() {
        return priceAtOrder.multiply(BigDecimal.valueOf(quantity));
    }

    public boolean hasReturnRequest() {
        return returnStatus != null && !returnStatus.isBlank();
    }
}
