package com.clinic.order.dto;

import jakarta.validation.constraints.NotNull;

public class AppointmentRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Long slotId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }
}
