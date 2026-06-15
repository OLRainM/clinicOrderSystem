package com.clinic.order.appointment.service;

import com.clinic.order.appointment.repository.AppointmentOrderRepository;
import com.clinic.order.schedule.repository.ScheduleRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

@Service
public class AppointmentTimeoutService {
    private final AppointmentOrderRepository orderRepository;
    private final ScheduleRepository scheduleRepository;
    private final RedisQuotaService redisQuotaService;
    private final TransactionTemplate transactionTemplate;

    public AppointmentTimeoutService(AppointmentOrderRepository orderRepository, ScheduleRepository scheduleRepository,
                                     RedisQuotaService redisQuotaService, TransactionTemplate transactionTemplate) {
        this.orderRepository = orderRepository;
        this.scheduleRepository = scheduleRepository;
        this.redisQuotaService = redisQuotaService;
        this.transactionTemplate = transactionTemplate;
    }

    @Scheduled(fixedDelay = 60000)
    public void closeExpiredPendingOrders() {
        for (Map<String, Object> order : orderRepository.findExpiredPending(100)) {
            transactionTemplate.executeWithoutResult(status -> closeOne(order));
        }
    }

    public void closeOne(Map<String, Object> order) {
        Long orderId = ((Number) order.get("id")).longValue();
        Long userId = ((Number) order.get("user_id")).longValue();
        Long slotId = ((Number) order.get("slot_id")).longValue();
        if (orderRepository.closeExpired(orderId) == 1) {
            scheduleRepository.increaseMysqlQuota(slotId);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { redisQuotaService.rollback(userId, slotId); }
            });
        }
    }
}
