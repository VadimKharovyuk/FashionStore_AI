// ── Order.java ────────────────────────────────────────────────────────
package com.example.fashionstore_ai.model;

import com.example.fashionstore_ai.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_session_id",    columnList = "session_id"),
        @Index(name = "idx_order_number",         columnList = "order_number"),
        @Index(name = "idx_order_status",         columnList = "status")
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // прив'язка до сесії — той самий UUID що і в Cart / ChatSession
    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    // людино-читабельний номер замовлення — для SupportAgent
    // формат: ORD-2026-001234
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status;

    @OneToMany(mappedBy = "order",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    // ── Доставка ──────────────────────────────────────────────────
    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "recipient_name", length = 200)
    private String recipientName;

    @Column(name = "recipient_phone", length = 50)
    private String recipientPhone;

    @Column(name = "recipient_email", length = 200)
    private String recipientEmail;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "estimated_delivery")
    private LocalDate estimatedDelivery;

    // ── Нотатки для SupportAgent ──────────────────────────────────
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ── Timestamps ────────────────────────────────────────────────
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Helpers ───────────────────────────────────────────────────

    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems() {
        return items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    // SupportAgent перевіряє чи можна скасувати замовлення
    public boolean isCancellable() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    // SupportAgent перевіряє чи можна змінити адресу
    public boolean isAddressChangeable() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    // SupportAgent перевіряє чи можна створити повернення
    public boolean isReturnable() {
        return status == OrderStatus.DELIVERED;
    }
}
