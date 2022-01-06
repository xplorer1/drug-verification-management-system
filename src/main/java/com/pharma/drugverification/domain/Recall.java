package com.pharma.drugverification.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "recalls", indexes = {
        @Index(name = "idx_recall_number", columnList = "recallNumber", unique = true),
        @Index(name = "idx_batch_id", columnList = "batchId"),
        @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Recall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 100)
    private String recallNumber;

    @NotNull
    @Column(nullable = false)
    private Long batchId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batchId", insertable = false, updatable = false)
    private Batch batch;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String reason;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RecallClassification classification;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RecallStatus status = RecallStatus.ACTIVE;

    @Column(nullable = false)
    private Integer affectedUnits = 0;

    @Column(nullable = false)
    private Integer recoveredUnits = 0;

    @Column
    private Double effectiveness;

    @NotNull
    @Column(nullable = false)
    private Long initiatedByUserId;

    @Column
    private Long closedByUserId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime closedAt;

    public enum RecallClassification {
        CLASS_I,
        CLASS_II,
        CLASS_III
    }

    public enum RecallStatus {
        ACTIVE,
        COMPLETED,
        CLOSED
    }
}
