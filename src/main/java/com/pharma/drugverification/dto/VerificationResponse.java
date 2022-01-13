package com.pharma.drugverification.dto;

import com.pharma.drugverification.domain.VerificationRequest.VerificationResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponse {

    private VerificationResult result;
    private String serialNumber;
    private String drugName;
    private String batchNumber;
    private String manufacturer;
    private LocalDateTime expirationDate;
    private Boolean isValid;
    private List<String> warnings = new ArrayList<>();
    private String message;
    private Long responseTimeMs;
    private LocalDateTime verifiedAt;

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
}
