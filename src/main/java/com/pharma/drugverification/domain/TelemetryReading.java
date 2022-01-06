package com.pharma.drugverification.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "telemetry_readings", indexes = {
        @Index(name = "idx_batch_id_timestamp", columnList = "batchId,timestamp"),
        @Index(name = "idx_device_id", columnList = "deviceId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long batchId;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(precision = 5, scale = 2)
    private BigDecimal humidity;

    @Column(length = 255)
    private String location;

    @Column(length = 100)
    private String deviceId;

    @Column
    private Double latitude;

    @Column
    private Double longitude;
}
