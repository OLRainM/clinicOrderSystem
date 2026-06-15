package com.clinic.order.common.config;

import com.clinic.order.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

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
        return ApiResponse.ok("Redis号源初始化完成", Map.of("slotCount", initService.initRedisQuotaFromMysql()));
    }
}
