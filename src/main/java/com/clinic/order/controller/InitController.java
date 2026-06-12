package com.clinic.order.controller;

import com.clinic.order.dto.ApiResponse;
import com.clinic.order.service.InitService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/init")
public class InitController {
    private final InitService initService;

    public InitController(InitService initService) {
        this.initService = initService;
    }

    @PostMapping("/redis-quota")
    public ApiResponse<Map<String, Object>> redisQuota() {
        int count = initService.initRedisQuotaFromMysql();
        return ApiResponse.ok("Redis号源初始化完成", Map.of("slotCount", count));
    }
}
