package com.clinic.order.appointment.dto;

import jakarta.validation.constraints.NotNull;

public class ReserveRequest {
    @NotNull
    private Long slotId;

    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }
}
