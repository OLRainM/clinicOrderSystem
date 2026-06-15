package com.clinic.order.stats.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class StatsAggregationTask {
    private final StatsService statsService;

    public StatsAggregationTask(StatsService statsService) {
        this.statsService = statsService;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void aggregateYesterday() {
        statsService.aggregate(LocalDate.now().minusDays(1));
    }
}
