package com.factory.mes.controller;

import com.factory.mes.dto.EquipmentStatusDto;
import com.factory.mes.entity.EquipmentEvent;
import com.factory.mes.service.EquipmentQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EventController {

    private final EquipmentQueryService queryService;

    public EventController(EquipmentQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/events")
    public ResponseEntity<List<EquipmentEvent>> getEvents(@RequestParam String equipmentId) {
        return ResponseEntity.ok(queryService.getEventsByEquipmentId(equipmentId));
    }

    @GetMapping("/events/alerts")
    public ResponseEntity<List<EquipmentEvent>> getAlerts() {
        return ResponseEntity.ok(queryService.getAlertEvents());
    }

    @GetMapping("/events/warnings")
    public ResponseEntity<List<EquipmentEvent>> getWarnings() {
        return ResponseEntity.ok(queryService.getWarningEvents());
    }

    @GetMapping("/equipments/status")
    public ResponseEntity<List<EquipmentStatusDto>> getStatus() {
        return ResponseEntity.ok(queryService.getEquipmentStatus());
    }

    @GetMapping("/equipments/status/by-site/{siteId}")
    public ResponseEntity<List<EquipmentStatusDto>> getStatusBySite(@PathVariable Long siteId) {
        return ResponseEntity.ok(queryService.getEquipmentStatusBySite(siteId));
    }

    @GetMapping("/equipments/status/by-process-main/{processMainId}")
    public ResponseEntity<List<EquipmentStatusDto>> getStatusByProcessMain(@PathVariable Long processMainId) {
        return ResponseEntity.ok(queryService.getEquipmentStatusByProcessMain(processMainId));
    }

    @GetMapping("/equipments/status/by-line/{lineId}")
    public ResponseEntity<List<EquipmentStatusDto>> getStatusByLine(@PathVariable Long lineId) {
        return ResponseEntity.ok(queryService.getEquipmentStatusByLine(lineId));
    }

    @GetMapping("/equipments/status/by-process-sub/{processSubId}")
    public ResponseEntity<List<EquipmentStatusDto>> getStatusByProcessSub(@PathVariable Long processSubId) {
        return ResponseEntity.ok(queryService.getEquipmentStatusByProcessSub(processSubId));
    }
}
