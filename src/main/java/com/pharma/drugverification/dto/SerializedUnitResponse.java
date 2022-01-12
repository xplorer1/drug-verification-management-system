package com.pharma.drugverification.dto;

import com.pharma.drugverification.domain.SerializedUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SerializedUnitResponse {

    private Long id;
    private String serialNumber;
    private Long batchId;
    private String batchNumber;
    private String gtin;
    private String cryptoTail;
    private String dataMatrix;
    private Integer keyVersion;
    private SerializedUnit.UnitStatus status;
    private Long parentAggregationId;
    private LocalDateTime dispensedAt;
    private Long dispensedByUserId;
    private String dispensedByPharmacy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SerializedUnitResponse from(SerializedUnit unit) {
        SerializedUnitResponse response = new SerializedUnitResponse();
        response.setId(unit.getId());
        response.setSerialNumber(unit.getSerialNumber());
        response.setBatchId(unit.getBatchId());
        response.setBatchNumber(unit.getBatch() != null ? unit.getBatch().getBatchNumber() : null);
        response.setGtin(unit.getGtin());
        response.setCryptoTail(unit.getCryptoTail());
        response.setDataMatrix(unit.getDataMatrix());
        response.setKeyVersion(unit.getKeyVersion());
        response.setStatus(unit.getStatus());
        response.setParentAggregationId(unit.getParentAggregationId());
        response.setDispensedAt(unit.getDispensedAt());
        response.setDispensedByUserId(unit.getDispensedByUserId());
        response.setDispensedByPharmacy(unit.getDispensedByPharmacy());
        response.setCreatedAt(unit.getCreatedAt());
        response.setUpdatedAt(unit.getUpdatedAt());
        return response;
    }
}
