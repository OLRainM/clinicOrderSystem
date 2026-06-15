package com.clinic.order.appointment.model;

public enum AppointmentStatus {
    PENDING_PAY(0), PAID(1), CANCELLED(2), RESCHEDULED(3);

    private final int code;

    AppointmentStatus(int code) { this.code = code; }
    public int getCode() { return code; }
}
