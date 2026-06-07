package com.factory.mes.service;

import com.factory.mes.dto.SpecDto;
import com.factory.mes.entity.Equipment;
import com.factory.mes.entity.EquipmentSpec;
import com.factory.mes.entity.ProcessSub;
import com.factory.mes.repository.EquipmentRepository;
import com.factory.mes.repository.EquipmentSpecRepository;
import com.factory.mes.repository.ProcessSubRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SpecService {

    private final EquipmentSpecRepository repository;
    private final EquipmentRepository equipmentRepository;
    private final ProcessSubRepository processSubRepository;

    public SpecService(EquipmentSpecRepository repository,
                       EquipmentRepository equipmentRepository,
                       ProcessSubRepository processSubRepository) {
        this.repository = repository;
        this.equipmentRepository = equipmentRepository;
        this.processSubRepository = processSubRepository;
    }

    @Transactional(readOnly = true)
    public List<SpecDto> getAll() {
        return repository.findAll().stream()
                .map(s -> new SpecDto(s.getId(), s.getEquipmentId(), s.getEventType(),
                        s.getThreshold(), s.getTolerancePct()))
                .toList();
    }

    @Transactional
    public SpecDto save(SpecDto dto) {
        // 새 장비인데 processSubId가 제공된 경우 → equipment 테이블에 자동 등록
        if (dto.id() == null && dto.processSubId() != null) {
            boolean exists = equipmentRepository.existsById(dto.equipmentId());
            if (!exists) {
                ProcessSub ps = processSubRepository.findById(dto.processSubId())
                        .orElseThrow(() -> new IllegalArgumentException("소공정을 찾을 수 없습니다."));
                Equipment eq = new Equipment();
                eq.setEquipmentId(dto.equipmentId());
                eq.setName(dto.equipmentName() != null ? dto.equipmentName() : dto.equipmentId());
                eq.setProcessSub(ps);
                equipmentRepository.save(eq);
            }
        }

        EquipmentSpec spec = repository
                .findByEquipmentIdAndEventType(dto.equipmentId(), dto.eventType())
                .orElse(new EquipmentSpec());

        spec.setEquipmentId(dto.equipmentId());
        spec.setEventType(dto.eventType());
        spec.setThreshold(dto.threshold());
        spec.setTolerancePct(dto.tolerancePct());
        EquipmentSpec saved = repository.save(spec);

        return new SpecDto(saved.getId(), saved.getEquipmentId(), saved.getEventType(),
                saved.getThreshold(), saved.getTolerancePct());
    }

    @Transactional(readOnly = true)
    public EquipmentSpec findSpec(String equipmentId, String eventType) {
        return repository.findByEquipmentIdAndEventType(equipmentId, eventType).orElse(null);
    }
}
