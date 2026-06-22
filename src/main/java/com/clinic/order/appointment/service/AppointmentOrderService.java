package com.clinic.order.appointment.service;

import com.clinic.order.appointment.model.AppointmentStatus;
import com.clinic.order.appointment.repository.AppointmentOrderRepository;
import com.clinic.order.schedule.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentOrderService {
    private final AppointmentAsyncService asyncService;
    private final AppointmentOrderRepository orderRepository;
    private final ScheduleRepository scheduleRepository;
    private final RedisQuotaService redisQuotaService;
    private final RefundService refundService;

    @Value("${clinic.lock-minutes:10}")
    private long lockMinutes;

    public AppointmentOrderService(AppointmentAsyncService asyncService, AppointmentOrderRepository orderRepository,
                                   ScheduleRepository scheduleRepository, RedisQuotaService redisQuotaService,
                                   RefundService refundService) {
        this.asyncService = asyncService;
        this.orderRepository = orderRepository;
        this.scheduleRepository = scheduleRepository;
        this.redisQuotaService = redisQuotaService;
        this.refundService = refundService;
    }

    public Map<String, Object> reserve(Long userId, Long slotId) {
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(lockMinutes);
        long expireMillis = expireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Long result = redisQuotaService.deduct(userId, slotId, expireMillis, lockMinutes * 60);
        if (result == null || result == -3) return Map.of("reserved", false, "reason", "号源初始化中，请重试");
        if (result == -2) return Map.of("reserved", false, "reason", "请勿重复预约同一时段");
        if (result == -1) return Map.of("reserved", false, "reason", "该时段号源已满");
        String orderNo = OrderNoGenerator.next();
        asyncService.createPendingOrder(orderNo, userId, slotId, expireTime);
        return Map.of("reserved", true, "orderNo", orderNo, "message", "抢号成功，请在10分钟内支付");
    }

    public List<Map<String, Object>> listMyOrders(Long userId) {
        return orderRepository.findOwnerOrders(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean pay(Long orderId, Long userId) {
        Map<String, Object> order = orderRepository.findOwnerOrder(orderId, userId).orElseThrow();
        boolean paid = orderRepository.markPaid(orderId, userId) == 1;
        if (paid) afterCommit(() -> redisQuotaService.unlock(userId, (Long) order.get("slotId")));
        return paid;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean pay(String orderNo, Long userId) {
        Map<String, Object> order = orderRepository.findOwnerOrder(orderNo, userId).orElseThrow();
        boolean paid = orderRepository.markPaid(orderNo, userId) == 1;
        if (paid) afterCommit(() -> redisQuotaService.unlock(userId, (Long) order.get("slotId")));
        return paid;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(Long orderId, Long userId) {
        Map<String, Object> order = orderRepository.findOwnerOrder(orderId, userId).orElseThrow();
        return cancelLoadedOrder(null, orderId, userId, order);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(String orderNo, Long userId) {
        Map<String, Object> order = orderRepository.findOwnerOrder(orderNo, userId).orElseThrow();
        return cancelLoadedOrder(orderNo, null, userId, order);
    }

    private boolean cancelLoadedOrder(String orderNo, Long orderId, Long userId, Map<String, Object> order) {
        int status = (Integer) order.get("status");
        Long slotId = (Long) order.get("slotId");
        if (status == AppointmentStatus.PAID.getCode()) {
            LocalDateTime visitTime = scheduleRepository.findSlotStartDateTime(slotId);
            if (visitTime.minusHours(2).isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("距离出诊不足2小时，不能取消已支付预约");
            }
            if (!refundService.requestRefund(String.valueOf(order.get("orderNo")), userId)) {
                throw new IllegalStateException("退款申请失败，取消终止");
            }
        }
        if (status != AppointmentStatus.PENDING_PAY.getCode() && status != AppointmentStatus.PAID.getCode()) return false;
        int updated = orderNo == null ? orderRepository.cancel(orderId, userId, status) : orderRepository.cancel(orderNo, userId, status);
        if (updated != 1) return false;
        scheduleRepository.increaseMysqlQuota(slotId);
        afterCommit(() -> redisQuotaService.rollback(userId, slotId));
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> reschedule(Long userId, String oldOrderNo, Long newSlotId) {
        Map<String, Object> old = orderRepository.findOwnerOrder(oldOrderNo, userId).orElseThrow();
        if ((Integer) old.get("status") != AppointmentStatus.PAID.getCode()) return Map.of("success", false, "message", "只有已支付订单可以改签");
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(lockMinutes);
        long expireMillis = expireTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Long result = redisQuotaService.deduct(userId, newSlotId, expireMillis, lockMinutes * 60);
        if (result == null || result < 1) return Map.of("success", false, "message", "新时段号源不足，原预约仍有效");
        String newOrderNo = OrderNoGenerator.next();
        try {
            Long oldSlotId = (Long) old.get("slotId");
            orderRepository.create(newOrderNo, userId, newSlotId, AppointmentStatus.PAID.getCode(), oldOrderNo, expireTime);
            if (orderRepository.markRescheduled(oldOrderNo, userId) != 1) throw new IllegalStateException("旧单状态异常");
            if (scheduleRepository.decreaseMysqlQuota(newSlotId) != 1) throw new IllegalStateException("新时段库存不足");
            scheduleRepository.increaseMysqlQuota(oldSlotId);
            afterCommit(() -> {
                redisQuotaService.rollback(userId, oldSlotId);
                redisQuotaService.unlock(userId, newSlotId);
            });
            return Map.of("success", true, "newOrderNo", newOrderNo);
        } catch (Exception ex) {
            redisQuotaService.rollback(userId, newSlotId);
            throw ex;
        }
    }

    private void afterCommit(Runnable runnable) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { runnable.run(); }
        });
    }
}
