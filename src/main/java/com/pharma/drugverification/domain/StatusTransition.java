package com.pharma.drugverification.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "status_transitions", indexes = {
        @Index(name = "idx_entity_type_id", columnList = "entityType,entityId"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class StatusTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EntityType entityType;

    @NotNull
    @Column(nullable = false)
    private Long entityId;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String fromStatus;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String toStatus;

    @Column(length = 1000)
    private String reason;

    @NotNull
    @Column(nullable = false)
    private Long changedByUserId;

    @Column(length = 100)
    private String changedByUsername;

    @Column(length = 500)
    private String metadata;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum EntityType {
        DRUG,
        BATCH,
        SERIALIZED_UNIT
    }
}
