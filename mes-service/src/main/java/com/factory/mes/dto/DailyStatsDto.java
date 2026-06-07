package com.factory.mes.dto;

import java.util.List;

public class DailyStatsDto {

    public record SiteStats(
            Long siteId,
            String siteName,
            String siteLocation,
            double avgOperationRate,
            double yieldRate,
            long alertCount,
            long warningCount,
            List<ProcessMainStats> processMainList
    ) {}

    public record ProcessMainStats(
            Long processMainId,
            String processMainName,
            double avgOperationRate,
            double yieldRate,
            long alertCount,
            long warningCount
    ) {}
}
