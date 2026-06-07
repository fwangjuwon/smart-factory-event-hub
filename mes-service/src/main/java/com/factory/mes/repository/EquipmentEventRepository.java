package com.factory.mes.repository;

import com.factory.mes.entity.EquipmentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EquipmentEventRepository extends JpaRepository<EquipmentEvent, Long> {

    List<EquipmentEvent> findByEquipmentIdOrderByCreatedAtDesc(String equipmentId);

    List<EquipmentEvent> findByStatusOrderByCreatedAtDesc(String status);

    @Query("SELECT DISTINCT e.equipmentId FROM EquipmentEvent e")
    List<String> findDistinctEquipmentIds();

    Optional<EquipmentEvent> findTopByEquipmentIdAndEventTypeOrderByCreatedAtDesc(
            String equipmentId, String eventType);

    @Query("""
            SELECT e FROM EquipmentEvent e
            WHERE e.equipmentId = :equipmentId
              AND e.createdAt = (
                SELECT MAX(e2.createdAt) FROM EquipmentEvent e2
                WHERE e2.equipmentId = :equipmentId AND e2.eventType = e.eventType
              )
            """)
    List<EquipmentEvent> findLatestEventsByEquipmentId(@Param("equipmentId") String equipmentId);
}
