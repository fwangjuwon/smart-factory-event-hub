package com.factory.mes.dto;

import java.time.LocalDateTime;

public record EquipmentEventMessage(
        String equipmentId,
        String eventType,
        double value,
        LocalDateTime timestamp
) {}
