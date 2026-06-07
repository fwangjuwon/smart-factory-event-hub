package com.factory.mes.repository;

import com.factory.mes.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SiteRepository extends JpaRepository<Site, Long> {

    @Query("SELECT DISTINCT s FROM Site s LEFT JOIN FETCH s.processMainList")
    List<Site> findAllWithProcessMain();

    @Query(nativeQuery = true, value = """
        SELECT
            s.id                AS siteId,
            s.name              AS siteName,
            s.location          AS siteLocation,
            pm.id               AS processMainId,
            pm.name             AS processMainName,
            AVG(CASE WHEN ee.event_type = 'OPERATION_RATE' THEN CAST(ee.value AS float8) END) AS avgOperationRate,
            AVG(CASE WHEN ee.event_type = 'DEFECT_RATE'    THEN CAST(ee.value AS float8) END) AS avgDefectRate,
            COUNT(CASE WHEN ee.status = 'ALERT'   THEN 1 END) AS alertCount,
            COUNT(CASE WHEN ee.status = 'WARNING' THEN 1 END) AS warningCount
        FROM site s
        JOIN process_main pm ON pm.site_id = s.id
        JOIN production_line pl ON pl.process_main_id = pm.id
        JOIN process_sub ps ON ps.production_line_id = pl.id
        JOIN equipment eq ON eq.process_sub_id = ps.id
        LEFT JOIN equipment_event ee
            ON ee.equipment_id = eq.equipment_id
            AND CAST(ee.created_at AS DATE) = CAST(:date AS DATE)
        GROUP BY s.id, s.name, s.location, pm.id, pm.name
        ORDER BY s.id, pm.id
        """)
    List<Object[]> findDailyStatsByProcessMain(@Param("date") String date);
}
