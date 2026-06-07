package com.factory.mes.dto;

import java.math.BigDecimal;

public record SpecDto(
        Long id,
        String equipmentId,
        String equipmentName,
        String eventType,
        BigDecimal threshold,
        BigDecimal tolerancePct,
        Long processSubId
) {
    public SpecDto(Long id, String equipmentId, String eventType, BigDecimal threshold, BigDecimal tolerancePct) {
        this(id, equipmentId, null, eventType, threshold, tolerancePct, null);
    }
}
