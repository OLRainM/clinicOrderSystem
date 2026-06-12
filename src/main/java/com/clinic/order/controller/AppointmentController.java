package com.clinic.order.controller;

import com.clinic.order.dto.ApiResponse;
import com.clinic.order.dto.AppointmentRequest;
import com.clinic.order.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/reserve")
    public ApiResponse<Map<String, Object>> reserve(@RequestBody @Valid AppointmentRequest request) {
        Map<String, Object> result = appointmentService.reserve(request.getUserId(), request.getSlotId());
        boolean ok = Boolean.TRUE.equals(result.get("reserved"));
        return ok ? ApiResponse.ok("预约锁号成功", result) : ApiResponse.fail(String.valueOf(result.get("reason")));
    }

    @PostMapping("/{appointmentId}/pay")
    public ApiResponse<Void> pay(@PathVariable Long appointmentId, @RequestParam Long userId) {
        return appointmentService.pay(appointmentId, userId) ? ApiResponse.ok("支付成功", null) : ApiResponse.fail("支付失败或订单已超时");
    }

    @PostMapping("/{appointmentId}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long appointmentId, @RequestParam Long userId) {
        return appointmentService.cancel(appointmentId, userId) ? ApiResponse.ok("取消成功，号源已回滚", null) : ApiResponse.fail("取消失败");
    }
}
