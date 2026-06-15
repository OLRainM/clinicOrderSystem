package com.clinic.order.appointment.controller;

import com.clinic.order.appointment.dto.ReserveRequest;
import com.clinic.order.appointment.dto.RescheduleRequest;
import com.clinic.order.appointment.service.AppointmentOrderService;
import com.clinic.order.common.dto.ApiResponse;
import com.clinic.order.common.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class AppointmentOrderController {
    private final AppointmentOrderService appointmentOrderService;

    public AppointmentOrderController(AppointmentOrderService appointmentOrderService) {
        this.appointmentOrderService = appointmentOrderService;
    }

    @PostMapping("/reserve")
    public ApiResponse<Map<String, Object>> reserve(@RequestBody @Valid ReserveRequest request, HttpServletRequest servletRequest) {
        Long userId = SecurityUtils.currentUserId(servletRequest);
        Map<String, Object> result = appointmentOrderService.reserve(userId, request.getSlotId());
        return Boolean.TRUE.equals(result.get("reserved")) ? ApiResponse.ok("预约锁号成功", result) : ApiResponse.fail(String.valueOf(result.get("reason")));
    }

    @PostMapping("/{orderId}/pay")
    public ApiResponse<Void> pay(@PathVariable Long orderId, HttpServletRequest request) {
        return appointmentOrderService.pay(orderId, SecurityUtils.currentUserId(request)) ? ApiResponse.ok("支付成功", null) : ApiResponse.fail("支付失败或订单已超时");
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long orderId, HttpServletRequest request) {
        return appointmentOrderService.cancel(orderId, SecurityUtils.currentUserId(request)) ? ApiResponse.ok("取消成功", null) : ApiResponse.fail("取消失败");
    }

    @PostMapping("/reschedule")
    public ApiResponse<Map<String, Object>> reschedule(@RequestBody @Valid RescheduleRequest req, HttpServletRequest request) {
        Map<String, Object> result = appointmentOrderService.reschedule(SecurityUtils.currentUserId(request), req.getOldOrderNo(), req.getNewSlotId());
        return Boolean.TRUE.equals(result.get("success")) ? ApiResponse.ok("改签成功", result) : ApiResponse.fail(String.valueOf(result.get("message")));
    }
}
