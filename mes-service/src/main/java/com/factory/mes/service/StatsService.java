package com.factory.mes.service;

import com.factory.mes.dto.DailyStatsDto;
import com.factory.mes.repository.SiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StatsService {

    private final SiteRepository siteRepository;

    public StatsService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    public List<DailyStatsDto.SiteStats> getDailyStats(LocalDate date) {
        List<Object[]> rows = siteRepository.findDailyStatsByProcessMain(date.toString());

        // site_id 기준으로 그룹핑
        Map<Long, List<Object[]>> bySite = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Long siteId = toLong(row[0]);
            bySite.computeIfAbsent(siteId, k -> new ArrayList<>()).add(row);
        }

        return bySite.values().stream().map(siteRows -> {
            Object[] first = siteRows.get(0);
            Long   siteId       = toLong(first[0]);
            String siteName     = (String) first[1];
            String siteLocation = (String) first[2];

            List<DailyStatsDto.ProcessMainStats> pmList = siteRows.stream().map(r -> {
                Long   pmId   = toLong(r[3]);
                String pmName = (String) r[4];
                double opr    = toDouble(r[5]);
                double defect = toDouble(r[6]);
                long   alert  = toLong(r[7]);
                long   warn   = toLong(r[8]);
                return new DailyStatsDto.ProcessMainStats(pmId, pmName, opr, 100.0 - defect, alert, warn);
            }).collect(Collectors.toList());

            double siteOpr    = pmList.stream().mapToDouble(DailyStatsDto.ProcessMainStats::avgOperationRate).average().orElse(0);
            double siteYield  = pmList.stream().mapToDouble(DailyStatsDto.ProcessMainStats::yieldRate).average().orElse(0);
            long   siteAlert  = pmList.stream().mapToLong(DailyStatsDto.ProcessMainStats::alertCount).sum();
            long   siteWarn   = pmList.stream().mapToLong(DailyStatsDto.ProcessMainStats::warningCount).sum();

            return new DailyStatsDto.SiteStats(siteId, siteName, siteLocation,
                    round(siteOpr), round(siteYield), siteAlert, siteWarn, pmList);
        }).collect(Collectors.toList());
    }

    private Long toLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(v.toString());
    }

    private double toDouble(Object v) {
        if (v == null) return 0.0;
        if (v instanceof Number n) return n.doubleValue();
        return Double.parseDouble(v.toString());
    }

    private double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
