package com.clinic.order.appointment.service;

import com.clinic.order.appointment.model.AppointmentStatus;
import com.clinic.order.appointment.repository.AppointmentOrderRepository;
import com.clinic.order.schedule.repository.ScheduleRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AppointmentAsyncService {
    private final AppointmentOrderRepository orderRepository;
    private final ScheduleRepository scheduleRepository;
    private final RedisQuotaService redisQuotaService;

    public AppointmentAsyncService(AppointmentOrderRepository orderRepository,
                                   ScheduleRepository scheduleRepository,
                                   RedisQuotaService redisQuotaService) {
        this.orderRepository = orderRepository;
        this.scheduleRepository = scheduleRepository;
        this.redisQuotaService = redisQuotaService;
    }

    @Async("appointmentExecutor")
    @Transactional(rollbackFor = Exception.class)
    public void createPendingOrder(String orderNo, Long userId, Long slotId, LocalDateTime expireTime) {
        try {
            if (scheduleRepository.decreaseMysqlQuota(slotId) != 1) {
                throw new IllegalStateException("MySQL号源不足");
            }
            orderRepository.create(orderNo, userId, slotId, AppointmentStatus.PENDING_PAY.getCode(), null, expireTime);
        } catch (Exception ex) {
            redisQuotaService.rollback(userId, slotId);
            throw ex;
        }
    }
}
