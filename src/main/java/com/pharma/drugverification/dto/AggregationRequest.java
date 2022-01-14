package com.pharma.drugverification.dto;

import com.pharma.drugverification.domain.Aggregation;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AggregationRequest {

    @NotNull(message = "Aggregation type is required")
    private Aggregation.AggregationType type;

    @NotNull(message = "Serial number is required for parent")
    private String parentSerialNumber;

    @NotNull(message = "At least one child ID is required")
    private List<Long> childUnitIds;
}
