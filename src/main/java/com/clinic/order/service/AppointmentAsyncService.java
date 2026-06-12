package com.clinic.order.service;

import com.clinic.order.repository.AppointmentRepository;
import com.clinic.order.repository.ScheduleRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AppointmentAsyncService {
    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final StringRedisTemplate redisTemplate;
    private final ScheduleService scheduleService;

    public AppointmentAsyncService(AppointmentRepository appointmentRepository,
                                   ScheduleRepository scheduleRepository,
                                   StringRedisTemplate redisTemplate,
                                   ScheduleService scheduleService) {
        this.appointmentRepository = appointmentRepository;
        this.scheduleRepository = scheduleRepository;
        this.redisTemplate = redisTemplate;
        this.scheduleService = scheduleService;
    }

    @Async("appointmentExecutor")
    @Transactional(rollbackFor = Exception.class)
    public void createPendingOrder(Long userId, Long slotId, LocalDateTime expireTime) {
        try {
            int updated = scheduleRepository.decreaseMysqlQuota(slotId);
            if (updated != 1) {
                throw new IllegalStateException("MySQL 号源扣减失败");
            }
            appointmentRepository.createPending(userId, slotId, expireTime);
        } catch (Exception ex) {
            redisTemplate.opsForValue().increment(scheduleService.quotaKey(slotId));
            redisTemplate.opsForHash().delete(scheduleService.lockKey(slotId), String.valueOf(userId));
            throw ex;
        }
    }
}
