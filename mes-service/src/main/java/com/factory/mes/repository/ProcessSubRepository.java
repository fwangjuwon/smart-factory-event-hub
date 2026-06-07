package com.factory.mes.repository;

import com.factory.mes.entity.ProcessSub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProcessSubRepository extends JpaRepository<ProcessSub, Long> {

    @Query("SELECT DISTINCT ps FROM ProcessSub ps LEFT JOIN FETCH ps.equipments WHERE ps.productionLine.id = :lineId")
    List<ProcessSub> findByLineIdWithEquipments(@Param("lineId") Long lineId);
}
