package com.clinic.order.common.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class InitService {
    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    public InitService(JdbcTemplate jdbcTemplate, StringRedisTemplate redisTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
    }

    public int initRedisQuotaFromMysql() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT id, available_quota FROM schedule_slot");
        for (Map<String, Object> row : rows) {
            String slotId = String.valueOf(row.get("id"));
            redisTemplate.opsForValue().set("hospital:slot:quota:" + slotId, String.valueOf(row.get("available_quota")), Duration.ofHours(24));
            redisTemplate.delete("hospital:slot:lock:" + slotId);
        }
        return rows.size();
    }
}
