package com.factory.mes.repository;

import com.factory.mes.entity.EquipmentSpec;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentSpecRepository extends JpaRepository<EquipmentSpec, Long> {
    Optional<EquipmentSpec> findByEquipmentIdAndEventType(String equipmentId, String eventType);
    List<EquipmentSpec> findByEquipmentId(String equipmentId);
}
