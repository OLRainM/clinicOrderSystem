package com.clinic.order.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RescheduleRequest {
    @NotBlank
    private String oldOrderNo;
    @NotNull
    private Long newSlotId;

    public String getOldOrderNo() { return oldOrderNo; }
    public void setOldOrderNo(String oldOrderNo) { this.oldOrderNo = oldOrderNo; }
    public Long getNewSlotId() { return newSlotId; }
    public void setNewSlotId(Long newSlotId) { this.newSlotId = newSlotId; }
}
