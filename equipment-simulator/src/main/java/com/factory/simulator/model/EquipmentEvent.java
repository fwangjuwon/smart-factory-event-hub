package com.factory.simulator.model;

import java.time.LocalDateTime;

public record EquipmentEvent(
        String equipmentId,
        String eventType,
        double value,
        LocalDateTime timestamp
) {}
