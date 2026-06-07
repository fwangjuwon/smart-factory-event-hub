package com.factory.mes.repository;

import com.factory.mes.entity.ProcessMain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProcessMainRepository extends JpaRepository<ProcessMain, Long> {

    @Query("SELECT DISTINCT pm FROM ProcessMain pm LEFT JOIN FETCH pm.lines WHERE pm.site.id = :siteId")
    List<ProcessMain> findBySiteIdWithLines(@Param("siteId") Long siteId);
}
