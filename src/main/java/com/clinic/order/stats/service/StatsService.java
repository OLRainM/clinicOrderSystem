package com.clinic.order.stats.service;

import com.clinic.order.stats.repository.StatsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {
    private final StatsRepository repository;

    public StatsService(StatsRepository repository) {
        this.repository = repository;
    }

    public void aggregate(LocalDate date) {
        repository.aggregateDepartment(date);
        repository.aggregateDoctor(date);
    }

    public List<Map<String, Object>> departmentStats(LocalDate start, LocalDate end) {
        return repository.departmentStats(start, end);
    }

    public List<Map<String, Object>> doctorStats(LocalDate start, LocalDate end) {
        return repository.doctorStats(start, end);
    }
}
