package com.pharma.drugverification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AlertResolutionRequest {

    @NotBlank(message = "Resolution notes are required")
    private String resolutionNotes;
}
