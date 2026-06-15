package com.clinic.order.stats.controller;

import com.clinic.order.common.dto.ApiResponse;
import com.clinic.order.common.security.RequireRole;

import com.clinic.order.stats.service.StatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RequireRole(3)

@RestController
@RequestMapping("/admin/api/stats")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/department")
    public ApiResponse<List<Map<String, Object>>> department(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ApiResponse.ok("查询成功", statsService.departmentStats(start, end));
    }

    @GetMapping("/doctor")
    public ApiResponse<List<Map<String, Object>>> doctor(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ApiResponse.ok("查询成功", statsService.doctorStats(start, end));
    }

    @PostMapping("/aggregate")
    public ApiResponse<Void> aggregate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        statsService.aggregate(date);
        return ApiResponse.ok("聚合完成", null);
    }
}
