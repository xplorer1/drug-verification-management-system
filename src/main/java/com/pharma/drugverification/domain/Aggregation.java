package com.pharma.drugverification.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "aggregations", indexes = {
        @Index(name = "idx_parent_id", columnList = "parentId"),
        @Index(name = "idx_child_id", columnList = "childId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Aggregation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long parentId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AggregationType parentType;

    @NotNull
    @Column(nullable = false)
    private Long childId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AggregationType childType;

    @NotNull
    @Column(nullable = false)
    private Long batchId;

    @Column(nullable = false)
    private Boolean active = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime disaggregatedAt;

    @Column
    private Long disaggregatedByUserId;

    public enum AggregationType {
        UNIT,
        CASE,
        PALLET
    }
}
