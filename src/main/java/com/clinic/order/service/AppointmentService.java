package com.clinic.order.service;

import com.clinic.order.repository.AppointmentRepository;
import com.clinic.order.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentService {
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
    private final AppointmentAsyncService asyncService;
    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;

    @Value("${clinic.lock-minutes:10}")
    private long lockMinutes;

    public AppointmentService(StringRedisTemplate redisTemplate,
                              ScheduleService scheduleService,
                              AppointmentAsyncService asyncService,
                              AppointmentRepository appointmentRepository,
                              ScheduleRepository scheduleRepository) {
        this.redisTemplate = redisTemplate;
        this.scheduleService = scheduleService;
        this.asyncService = asyncService;
        this.appointmentRepository = appointmentRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public Map<String, Object> reserve(Long userId, Long slotId) {
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(lockMinutes);
        long expireMillis = expireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(LUA_DEDUCT, Long.class);
        Long result = redisTemplate.execute(script,
                List.of(scheduleService.quotaKey(slotId), scheduleService.lockKey(slotId)),
                String.valueOf(userId), String.valueOf(expireMillis), String.valueOf(lockMinutes * 60));

        if (result == null || result == -3) {
            scheduleService.syncQuotaIfAbsent(scheduleRepository.findSlot(slotId));
            return Map.of("reserved", false, "reason", "号源初始化中，请重试");
        }
        if (result == -2) return Map.of("reserved", false, "reason", "请勿重复预约同一时段");
        if (result == -1) return Map.of("reserved", false, "reason", "该时段号源已满");

        asyncService.createPendingOrder(userId, slotId, expireTime);
        return Map.of("reserved", true, "message", "抢号成功，请在10分钟内支付", "expireTime", expireTime.toString());
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean pay(Long appointmentId, Long userId) {
        int updated = appointmentRepository.markPaid(appointmentId, userId);
        if (updated == 1) {
            Long slotId = appointmentRepository.findSlotId(appointmentId);
            redisTemplate.opsForHash().delete(scheduleService.lockKey(slotId), String.valueOf(userId));
            return true;
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(Long appointmentId, Long userId) {
        Long slotId = appointmentRepository.findSlotId(appointmentId);
        int closed = appointmentRepository.closePending(appointmentId, userId);
        if (closed == 1) {
            scheduleRepository.increaseMysqlQuota(slotId);
            redisTemplate.opsForValue().increment(scheduleService.quotaKey(slotId));
            redisTemplate.opsForHash().delete(scheduleService.lockKey(slotId), String.valueOf(userId));
            return true;
        }
        return false;
    }
}
