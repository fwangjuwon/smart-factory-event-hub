package com.factory.mes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EquipmentStatusDto(
        String equipmentId,
        String equipmentName,
        String overallStatus,
        BigDecimal latestTemperature,
        BigDecimal latestOperationRate,
        BigDecimal latestDefectRate,
        LocalDateTime lastUpdated,
        // 계층 경로
        String siteName,
        String processMainName,
        String lineName,
        String processSubName
) {}
