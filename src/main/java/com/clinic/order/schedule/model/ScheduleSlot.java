package com.clinic.order.schedule.model;

import java.time.LocalTime;

public class ScheduleSlot {
    private Long id;
    private Long scheduleId;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer totalQuota;
    private Integer availableQuota;
    private Integer version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public Integer getTotalQuota() { return totalQuota; }
    public void setTotalQuota(Integer totalQuota) { this.totalQuota = totalQuota; }
    public Integer getAvailableQuota() { return availableQuota; }
    public void setAvailableQuota(Integer availableQuota) { this.availableQuota = availableQuota; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
