package com.factory.mes.repository;

import com.factory.mes.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, String> {

    @Query("""
            SELECT e FROM Equipment e
            LEFT JOIN FETCH e.processSub ps
            LEFT JOIN FETCH ps.productionLine pl
            LEFT JOIN FETCH pl.processMain pm
            LEFT JOIN FETCH pm.site
            WHERE e.equipmentId = :equipmentId
            """)
    Optional<Equipment> findByIdWithHierarchy(@Param("equipmentId") String equipmentId);

    @Query("""
            SELECT e.equipmentId FROM Equipment e
            WHERE e.processSub.productionLine.processMain.site.id = :siteId
            """)
    List<String> findEquipmentIdsBySiteId(@Param("siteId") Long siteId);

    @Query("""
            SELECT e.equipmentId FROM Equipment e
            WHERE e.processSub.productionLine.processMain.id = :processMainId
            """)
    List<String> findEquipmentIdsByProcessMainId(@Param("processMainId") Long processMainId);

    @Query("""
            SELECT e.equipmentId FROM Equipment e
            WHERE e.processSub.productionLine.id = :lineId
            """)
    List<String> findEquipmentIdsByLineId(@Param("lineId") Long lineId);

    @Query("""
            SELECT e.equipmentId FROM Equipment e
            WHERE e.processSub.id = :processSubId
            """)
    List<String> findEquipmentIdsByProcessSubId(@Param("processSubId") Long processSubId);
}
