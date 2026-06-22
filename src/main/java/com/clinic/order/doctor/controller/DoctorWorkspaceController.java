package com.clinic.order.doctor.controller;

import com.clinic.order.common.dto.ApiResponse;
import com.clinic.order.common.security.RequireRole;
import com.clinic.order.common.security.SecurityUtils;
import com.clinic.order.doctor.dto.FinishVisitRequest;
import com.clinic.order.doctor.repository.DoctorWorkspaceRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RequireRole(2)
@RestController
@RequestMapping("/doctor/api/workspace")
public class DoctorWorkspaceController {
    private final DoctorWorkspaceRepository repository;

    public DoctorWorkspaceController(DoctorWorkspaceRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> profile(HttpServletRequest request) {
        Long userId = SecurityUtils.currentUserId(request);
        return repository.profile(userId).map(p -> ApiResponse.ok("查询成功", p)).orElseGet(() -> ApiResponse.fail("医生档案不存在"));
    }

    @GetMapping("/queue")
    public ApiResponse<List<Map<String, Object>>> queue(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                        HttpServletRequest request) {
        return ApiResponse.ok("查询成功", repository.queue(SecurityUtils.currentUserId(request), date == null ? LocalDate.now() : date));
    }

    @GetMapping("/orders/{orderNo}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable String orderNo, HttpServletRequest request) {
        Long userId = SecurityUtils.currentUserId(request);
        return repository.orderDetail(userId, orderNo).map(d -> ApiResponse.ok("查询成功", d)).orElseGet(() -> ApiResponse.fail("订单不存在或无权接诊"));
    }

    @PostMapping("/finish")
    public ApiResponse<Map<String, Object>> finish(@RequestBody @Valid FinishVisitRequest req, HttpServletRequest request) {
        Long recordId = repository.finishVisit(SecurityUtils.currentUserId(request), req);
        return ApiResponse.ok("问诊已完结", Map.of("recordId", recordId));
    }
}
