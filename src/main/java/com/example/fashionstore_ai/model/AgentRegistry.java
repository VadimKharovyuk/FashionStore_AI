package com.example.fashionstore_ai.model;

import com.example.fashionstore_ai.enums.AgentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent_registry")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_type", nullable = false)
    private AgentType agentType;



    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(name = "embedding", columnDefinition = "vector(1024)")
    private float[] embedding;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String competencies;

    @Column(name = "trigger_examples", columnDefinition = "TEXT")
    private String triggerExamples;

    @Column(name = "not_responsible_for", columnDefinition = "TEXT")
    private String notResponsibleFor;

    @Column(name = "bean_name", length = 200)
    private String beanName;

    @Column(name = "system_prompt_template", columnDefinition = "TEXT")
    private String systemPromptTemplate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "priority")
    private Integer priority = 1;

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