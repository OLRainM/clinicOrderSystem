package com.clinic.order.medical.controller;

import com.clinic.order.common.dto.ApiResponse;
import com.clinic.order.common.security.SecurityUtils;
import com.clinic.order.medical.service.MedicalRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/medical-records")
public class MedicalRecordController {
    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @GetMapping("/{recordId}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long recordId, HttpServletRequest request) {
        Long userId = SecurityUtils.currentUserId(request);
        return medicalRecordService.findOwnerRecord(recordId, userId)
                .map(data -> ApiResponse.ok("查询成功", data))
                .orElseGet(() -> ApiResponse.fail("病历不存在或无权访问"));
    }
}
