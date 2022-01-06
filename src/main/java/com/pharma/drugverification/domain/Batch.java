package com.pharma.drugverification.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "batches", indexes = {
        @Index(name = "idx_batch_number", columnList = "batchNumber", unique = true),
        @Index(name = "idx_drug_id", columnList = "drugId"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_expiration_date", columnList = "expirationDate")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true, length = 100)
    private String batchNumber;

    @NotNull
    @Column(nullable = false)
    private Long drugId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "drugId", insertable = false, updatable = false)
    private Drug drug;

    @NotNull
    @Column(nullable = false)
    private LocalDate manufacturingDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate expirationDate;

    @NotNull
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BatchStatus status = BatchStatus.ACTIVE;

    @Column(length = 200)
    private String location;

    @Column(length = 1000)
    private String notes;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum BatchStatus {
        ACTIVE,
        IN_TRANSIT,
        QUARANTINED,
        RECALLED,
        EXPIRED,
        DEPLETED
    }
}
