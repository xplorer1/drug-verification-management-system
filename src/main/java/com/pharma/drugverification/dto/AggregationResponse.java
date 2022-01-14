package com.pharma.drugverification.dto;

import com.pharma.drugverification.domain.Aggregation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AggregationResponse {

    private Long id;
    private Aggregation.AggregationType type;
    private String parentSerialNumber;
    private Integer childCount;
    private Boolean disaggregated;
    private LocalDateTime disaggregatedAt;
    private LocalDateTime createdAt;

    public static AggregationResponse from(Aggregation aggregation) {
        AggregationResponse response = new AggregationResponse();
        response.setId(aggregation.getId());
        response.setType(aggregation.getParentType());
        response.setParentSerialNumber(aggregation.getParentId() != null ? aggregation.getParentId().toString() : null);
        response.setChildCount(1); // Each aggregation record represents one parent-child relationship
        response.setDisaggregated(!aggregation.getActive());
        response.setDisaggregatedAt(aggregation.getDisaggregatedAt());
        response.setCreatedAt(aggregation.getCreatedAt());
        return response;
    }
}
