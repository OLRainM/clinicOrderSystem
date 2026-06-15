package com.clinic.order.appointment.service;

import com.clinic.order.schedule.repository.ScheduleRepository;
import com.clinic.order.schedule.service.ScheduleService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class RedisQuotaService {
    private static final String LUA_DEDUCT = """
        local quota = tonumber(redis.call('GET', KEYS[1]) or '-1')
        if quota < 0 then return -3 end
        if redis.call('HEXISTS', KEYS[2], ARGV[1]) == 1 then return -2 end
        if quota <= 0 then return -1 end
        redis.call('DECR', KEYS[1])
        redis.call('HSET', KEYS[2], ARGV[1], ARGV[2])
        redis.call('EXPIRE', KEYS[2], ARGV[3])
        return 1
        """;

    private final StringRedisTemplate redisTemplate;
    private final ScheduleService scheduleService;
    private final ScheduleRepository scheduleRepository;

    public RedisQuotaService(StringRedisTemplate redisTemplate, ScheduleService scheduleService, ScheduleRepository scheduleRepository) {
        this.redisTemplate = redisTemplate;
        this.scheduleService = scheduleService;
        this.scheduleRepository = scheduleRepository;
    }

    public Long deduct(Long userId, Long slotId, long expireMillis, long lockSeconds) {
        Long result = redisTemplate.execute(new DefaultRedisScript<>(LUA_DEDUCT, Long.class),
                List.of(scheduleService.quotaKey(slotId), scheduleService.lockKey(slotId)),
                String.valueOf(userId), String.valueOf(expireMillis), String.valueOf(lockSeconds));
        if (result != null && result == -3) scheduleService.syncQuotaIfAbsent(scheduleRepository.findSlot(slotId));
        return result;
    }

    public boolean acquireRateLimit(Long userId) {
        String key = scheduleService.rateKey(userId);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) redisTemplate.expire(key, Duration.ofSeconds(10));
        return count != null && count <= 5;
    }

    public void rollback(Long userId, Long slotId) {
        redisTemplate.opsForValue().increment(scheduleService.quotaKey(slotId));
        redisTemplate.opsForHash().delete(scheduleService.lockKey(slotId), String.valueOf(userId));
    }
}
