package com.factory.mes.service;

import com.factory.mes.dto.EquipmentStatusDto;
import com.factory.mes.entity.EquipmentEvent;
import com.factory.mes.repository.EquipmentEventRepository;
import com.factory.mes.repository.EquipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EquipmentQueryService {

    private final EquipmentEventRepository eventRepository;
    private final EquipmentRepository equipmentRepository;

    public EquipmentQueryService(EquipmentEventRepository eventRepository,
                                 EquipmentRepository equipmentRepository) {
        this.eventRepository = eventRepository;
        this.equipmentRepository = equipmentRepository;
    }

    public List<EquipmentEvent> getEventsByEquipmentId(String equipmentId) {
        return eventRepository.findByEquipmentIdOrderByCreatedAtDesc(equipmentId);
    }

    public List<EquipmentEvent> getAlertEvents() {
        return eventRepository.findByStatusOrderByCreatedAtDesc("ALERT");
    }

    public List<EquipmentEvent> getWarningEvents() {
        return eventRepository.findByStatusOrderByCreatedAtDesc("WARNING");
    }

    public List<EquipmentStatusDto> getEquipmentStatus() {
        return eventRepository.findDistinctEquipmentIds().stream()
                .map(this::buildStatus)
                .collect(Collectors.toList());
    }

    public List<EquipmentStatusDto> getEquipmentStatusBySite(Long siteId) {
        return buildStatusForIds(equipmentRepository.findEquipmentIdsBySiteId(siteId));
    }

    public List<EquipmentStatusDto> getEquipmentStatusByProcessMain(Long processMainId) {
        return buildStatusForIds(equipmentRepository.findEquipmentIdsByProcessMainId(processMainId));
    }

    public List<EquipmentStatusDto> getEquipmentStatusByLine(Long lineId) {
        return buildStatusForIds(equipmentRepository.findEquipmentIdsByLineId(lineId));
    }

    public List<EquipmentStatusDto> getEquipmentStatusByProcessSub(Long processSubId) {
        return buildStatusForIds(equipmentRepository.findEquipmentIdsByProcessSubId(processSubId));
    }

    private List<EquipmentStatusDto> buildStatusForIds(List<String> ids) {
        return ids.stream().map(this::buildStatus).collect(Collectors.toList());
    }

    private EquipmentStatusDto buildStatus(String equipmentId) {
        Map<String, EquipmentEvent> latestByType = List.of("TEMPERATURE", "OPERATION_RATE", "DEFECT_RATE")
                .stream()
                .flatMap(type -> eventRepository
                        .findTopByEquipmentIdAndEventTypeOrderByCreatedAtDesc(equipmentId, type)
                        .stream())
                .collect(Collectors.toMap(EquipmentEvent::getEventType, e -> e, (a, b) -> a));

        String overallStatus = latestByType.values().stream()
                .map(EquipmentEvent::getStatus)
                .reduce("NORMAL", this::worstStatus);

        LocalDateTime lastUpdated = latestByType.values().stream()
                .map(EquipmentEvent::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // 계층 경로 조회
        var eq       = equipmentRepository.findByIdWithHierarchy(equipmentId).orElse(null);
        String eqName     = eq != null ? eq.getName() : equipmentId;
        String siteName   = null, pmName = null, lineName = null, subName = null;
        if (eq != null && eq.getProcessSub() != null) {
            var ps  = eq.getProcessSub();
            var pl  = ps.getProductionLine();
            var pm  = pl.getProcessMain();
            var site= pm.getSite();
            subName  = ps.getName();
            lineName = pl.getName();
            pmName   = pm.getName();
            siteName = site.getName();
        }

        return new EquipmentStatusDto(
                equipmentId, eqName, overallStatus,
                getValue(latestByType, "TEMPERATURE"),
                getValue(latestByType, "OPERATION_RATE"),
                getValue(latestByType, "DEFECT_RATE"),
                lastUpdated,
                siteName, pmName, lineName, subName
        );
    }

    // ALERT > WARNING > NORMAL 우선순위
    private String worstStatus(String a, String b) {
        int rank = statusRank(a) >= statusRank(b) ? statusRank(a) : statusRank(b);
        return rank == 2 ? "ALERT" : rank == 1 ? "WARNING" : "NORMAL";
    }

    private int statusRank(String status) {
        return switch (status) {
            case "ALERT"   -> 2;
            case "WARNING" -> 1;
            default        -> 0;
        };
    }

    private BigDecimal getValue(Map<String, EquipmentEvent> map, String type) {
        EquipmentEvent e = map.get(type);
        return e != null ? e.getValue() : null;
    }
}
