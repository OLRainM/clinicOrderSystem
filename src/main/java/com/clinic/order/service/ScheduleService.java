package com.clinic.order.service;

import com.clinic.order.model.DoctorSchedule;
import com.clinic.order.model.ScheduleSlot;
import com.clinic.order.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final StringRedisTemplate redisTemplate;

    @Value("${clinic.schedule-cache-ttl-minutes:30}")
    private long scheduleTtlMinutes;

    public ScheduleService(ScheduleRepository scheduleRepository, StringRedisTemplate redisTemplate) {
        this.scheduleRepository = scheduleRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Cache Aside：排班列表以短 TTL 标记防穿透；号源库存使用独立 String，禁止塞大段 JSON。
     */
    public List<DoctorSchedule> querySchedules(Long departmentId, LocalDate date) {
        String cacheKey = "hospital:schedule:ready:" + (departmentId == null ? "all" : departmentId) + ":" + date;
        Boolean hit = redisTemplate.hasKey(cacheKey);

        List<DoctorSchedule> schedules = scheduleRepository.findSchedules(departmentId, date);
        for (DoctorSchedule schedule : schedules) {
            List<ScheduleSlot> slots = scheduleRepository.findSlotsByScheduleId(schedule.getId());
            for (ScheduleSlot slot : slots) {
                syncQuotaIfAbsent(slot);
            }
            schedule.setSlots(slots);
        }

        if (!Boolean.TRUE.equals(hit)) {
            redisTemplate.opsForValue().set(cacheKey, "1", Duration.ofMinutes(scheduleTtlMinutes));
        }
        return schedules;
    }

    public void syncQuotaIfAbsent(ScheduleSlot slot) {
        String quotaKey = quotaKey(slot.getId());
        Boolean exists = redisTemplate.hasKey(quotaKey);
        if (!Boolean.TRUE.equals(exists)) {
            redisTemplate.opsForValue().set(quotaKey, String.valueOf(slot.getAvailableQuota()), Duration.ofHours(24));
        }
    }

    public String quotaKey(Long slotId) {
        return "hospital:slot:quota:" + slotId;
    }

    public String lockKey(Long slotId) {
        return "hospital:slot:lock:" + slotId;
    }
}
