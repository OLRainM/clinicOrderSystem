package com.clinic.order.admin.controller;

import com.clinic.order.admin.repository.AdminRepository;
import com.clinic.order.common.dto.ApiResponse;
import com.clinic.order.common.security.RequireRole;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RequireRole(3)
@RestController
@RequestMapping("/admin/api/manage")
public class AdminManageController {
    private final AdminRepository repository;

    public AdminManageController(AdminRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/departments")
    public ApiResponse<List<Map<String, Object>>> departments() {
        return ApiResponse.ok("查询成功", repository.departments());
    }

    @PostMapping("/departments")
    public ApiResponse<Void> createDepartment(@RequestBody Map<String, Object> req) {
        repository.createDepartment(str(req, "name"));
        return ApiResponse.ok("科室已新增", null);
    }

    @PutMapping("/departments/{id}")
    public ApiResponse<Void> updateDepartment(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        repository.updateDepartment(id, str(req, "name"));
        return ApiResponse.ok("科室已更新", null);
    }

    @DeleteMapping("/departments/{id}")
    public ApiResponse<Void> deleteDepartment(@PathVariable Long id) {
        repository.deleteDepartment(id);
        return ApiResponse.ok("科室已删除", null);
    }


    @GetMapping("/doctors")
    public ApiResponse<List<Map<String, Object>>> doctors() {
        return ApiResponse.ok("查询成功", repository.doctors());
    }

    @PostMapping("/doctors")
    public ApiResponse<Void> createDoctor(@RequestBody Map<String, Object> req) {
        repository.createDoctor(num(req, "departmentId"), str(req, "name"), str(req, "title"));
        return ApiResponse.ok("医生已新增", null);
    }

    @PutMapping("/doctors/{id}")
    public ApiResponse<Void> updateDoctor(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        repository.updateDoctor(id, num(req, "departmentId"), str(req, "name"), str(req, "title"));
        return ApiResponse.ok("医生已更新", null);
    }

    @DeleteMapping("/doctors/{id}")
    public ApiResponse<Void> deleteDoctor(@PathVariable Long id) {
        repository.deleteDoctor(id);
        return ApiResponse.ok("医生已删除", null);
    }

    @GetMapping("/schedules")
    public ApiResponse<List<Map<String, Object>>> schedules(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok("查询成功", repository.schedules(date));
    }

    @PostMapping("/schedules")
    public ApiResponse<Map<String, Object>> createSchedule(@RequestBody Map<String, Object> req) {
        Long scheduleId = repository.createSchedule(num(req, "doctorId"), num(req, "departmentId"), LocalDate.parse(str(req, "scheduleDate")), str(req, "period"));
        return ApiResponse.ok("排班已新增", Map.of("scheduleId", scheduleId));
    }

    @PutMapping("/schedules/{id}")
    public ApiResponse<Void> updateSchedule(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        repository.updateSchedule(id, num(req, "doctorId"), num(req, "departmentId"), LocalDate.parse(str(req, "scheduleDate")), str(req, "period"));
        return ApiResponse.ok("排班已更新", null);
    }

    @DeleteMapping("/schedules/{id}")
    public ApiResponse<Void> deleteSchedule(@PathVariable Long id) {
        repository.deleteSchedule(id);
        return ApiResponse.ok("排班已删除", null);
    }

    @GetMapping("/schedules/{id}/slots")
    public ApiResponse<List<Map<String, Object>>> scheduleSlots(@PathVariable Long id) {
        return ApiResponse.ok("查询成功", repository.scheduleSlots(id));
    }

    @PostMapping("/schedules/with-slots")
    @SuppressWarnings("unchecked")
    public ApiResponse<Map<String, Object>> createScheduleWithSlots(@RequestBody Map<String, Object> req) {
        Long scheduleId = repository.createSchedule(num(req, "doctorId"), num(req, "departmentId"), LocalDate.parse(str(req, "scheduleDate")), str(req, "period"));
        List<Map<String, Object>> slots = (List<Map<String, Object>>) req.get("slots");
        if (slots != null) {
            for (Map<String, Object> slot : slots) {
                repository.createSlot(scheduleId, LocalTime.parse(String.valueOf(slot.get("startTime"))), LocalTime.parse(String.valueOf(slot.get("endTime"))), Integer.valueOf(String.valueOf(slot.get("totalQuota"))));
            }
        }
        return ApiResponse.ok("排班和时段已新增", Map.of("scheduleId", scheduleId));
    }


    @PostMapping("/slots")
    public ApiResponse<Void> createSlot(@RequestBody Map<String, Object> req) {
        repository.createSlot(num(req, "scheduleId"), LocalTime.parse(str(req, "startTime")), LocalTime.parse(str(req, "endTime")), integer(req, "totalQuota"));
        return ApiResponse.ok("时段已新增", null);
    }


    @PutMapping("/slots/{slotId}")
    public ApiResponse<Void> updateSlotQuota(@PathVariable Long slotId, @RequestBody Map<String, Object> req) {
        repository.updateSlotQuota(slotId, integer(req, "totalQuota"));
        return ApiResponse.ok("号源数量已更新", null);
    }

    @DeleteMapping("/slots/{slotId}")
    public ApiResponse<Void> deleteSlot(@PathVariable Long slotId) {
        repository.deleteSlot(slotId);
        return ApiResponse.ok("时段已删除", null);
    }

    private String str(Map<String, Object> req, String key) { return String.valueOf(req.get(key)); }
    private Long num(Map<String, Object> req, String key) { return Long.valueOf(String.valueOf(req.get(key))); }
    private Integer integer(Map<String, Object> req, String key) { return Integer.valueOf(String.valueOf(req.get(key))); }
}
