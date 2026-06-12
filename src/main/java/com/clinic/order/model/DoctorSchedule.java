package com.clinic.order.model;

import java.time.LocalDate;
import java.util.List;

public class DoctorSchedule {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private Long departmentId;
    private String departmentName;
    private LocalDate scheduleDate;
    private String period;
    private List<ScheduleSlot> slots;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public LocalDate getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(LocalDate scheduleDate) { this.scheduleDate = scheduleDate; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public List<ScheduleSlot> getSlots() { return slots; }
    public void setSlots(List<ScheduleSlot> slots) { this.slots = slots; }
}
