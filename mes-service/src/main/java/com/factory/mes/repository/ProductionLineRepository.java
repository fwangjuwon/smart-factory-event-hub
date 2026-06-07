package com.factory.mes.repository;

import com.factory.mes.entity.ProductionLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductionLineRepository extends JpaRepository<ProductionLine, Long> {

    @Query("SELECT DISTINCT l FROM ProductionLine l LEFT JOIN FETCH l.processSubs WHERE l.processMain.id = :pmId")
    List<ProductionLine> findByProcessMainIdWithSubs(@Param("pmId") Long pmId);
}
