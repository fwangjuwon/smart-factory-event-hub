package com.factory.mes.service;

import com.factory.mes.dto.EquipmentEventMessage;
import com.factory.mes.entity.EquipmentEvent;
import com.factory.mes.entity.EquipmentSpec;
import com.factory.mes.repository.EquipmentEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class EventProcessingService {

    private static final Logger log = LoggerFactory.getLogger(EventProcessingService.class);

    private final EquipmentEventRepository eventRepository;
    private final SpecService specService;

    public EventProcessingService(EquipmentEventRepository eventRepository, SpecService specService) {
        this.eventRepository = eventRepository;
        this.specService = specService;
    }

    @Transactional
    public void process(EquipmentEventMessage msg) {
        EquipmentSpec spec = specService.findSpec(msg.equipmentId(), msg.eventType());
        String status = determineStatus(msg.eventType(), msg.value(), spec);

        EquipmentEvent entity = new EquipmentEvent();
        entity.setEquipmentId(msg.equipmentId());
        entity.setEventType(msg.eventType());
        entity.setValue(BigDecimal.valueOf(msg.value()));
        entity.setStatus(status);
        entity.setCreatedAt(msg.timestamp());
        eventRepository.save(entity);

        switch (status) {
            case "ALERT"   -> log.warn("[ALERT]   {} - {} = {}", msg.equipmentId(), msg.eventType(), msg.value());
            case "WARNING" -> log.warn("[WARNING] {} - {} = {} (임계값 근접)", msg.equipmentId(), msg.eventType(), msg.value());
            default        -> log.info("[NORMAL]  {} - {} = {}", msg.equipmentId(), msg.eventType(), msg.value());
        }
    }

    private String determineStatus(String eventType, double value, EquipmentSpec spec) {
        if (spec == null) return defaultStatus(eventType, value);

        double threshold = spec.getThreshold().doubleValue();
        double margin    = threshold * spec.getTolerancePct().doubleValue() / 100.0;

        return switch (eventType) {
            case "TEMPERATURE" -> {
                if (value > threshold)          yield "ALERT";
                if (value > threshold - margin) yield "WARNING";
                yield "NORMAL";
            }
            case "OPERATION_RATE" -> {
                if (value < threshold)          yield "ALERT";
                if (value < threshold + margin) yield "WARNING";
                yield "NORMAL";
            }
            case "DEFECT_RATE" -> {
                if (value >= threshold)          yield "ALERT";
                if (value >= threshold - margin) yield "WARNING";
                yield "NORMAL";
            }
            default -> "NORMAL";
        };
    }

    private String defaultStatus(String eventType, double value) {
        return switch (eventType) {
            case "TEMPERATURE"    -> value > 80  ? "ALERT" : "NORMAL";
            case "OPERATION_RATE" -> value < 70  ? "ALERT" : "NORMAL";
            case "DEFECT_RATE"    -> value >= 3  ? "ALERT" : "NORMAL";
            default               -> "NORMAL";
        };
    }
}
