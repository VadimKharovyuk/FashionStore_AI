package com.example.fashionstore_ai.model;

import com.example.fashionstore_ai.enums.AgentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "tool_registry")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tool_name", length = 200, nullable = false)
    private String toolName;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(name = "embedding", columnDefinition = "vector(1024)")
    private float[] embedding;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "use_cases", columnDefinition = "TEXT")
    private String useCases;

    @Column(name = "not_use_cases", columnDefinition = "TEXT")
    private String notUseCases;

    @Column(name = "parameters_description", columnDefinition = "TEXT")
    private String parametersDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_agent", nullable = false)
    private AgentType ownerAgent;

    @Column(name = "bean_name", length = 200)
    private String beanName;

    @Column(name = "method_name", length = 200)
    private String methodName;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "requires_confirmation")
    private Boolean requiresConfirmation = false;

    @Column(name = "confirmation_message", columnDefinition = "TEXT")
    private String confirmationMessage;

    @Column(name = "estimated_cost_tokens")
    private Integer estimatedCostTokens;

    @Column(name = "version")
    private Integer version = 1;

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
}



//@JdbcTypeCode(SqlTypes.VECTOR)
//@Column(name = "embedding", columnDefinition = "vector(1024)")
//private List<Float> embedding;
