package com.factory.mes.controller;

import com.factory.mes.dto.DailyStatsDto;
import com.factory.mes.service.StatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/daily")
    public ResponseEntity<List<DailyStatsDto.SiteStats>> getDaily(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(statsService.getDailyStats(
                date != null ? date : LocalDate.now()));
    }
}
