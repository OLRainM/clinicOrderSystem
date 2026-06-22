package com.clinic.order.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class FinishVisitRequest {
    @NotBlank
    private String orderNo;
    @NotBlank
    private String symptoms;
    @NotBlank
    private String diagnosis;
    private List<Item> items;

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public static class Item {
        @NotBlank private String medicineName;
        @NotBlank private String dosage;
        @NotBlank private String usageInstruction;
        public String getMedicineName() { return medicineName; }
        public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }
        public String getUsageInstruction() { return usageInstruction; }
        public void setUsageInstruction(String usageInstruction) { this.usageInstruction = usageInstruction; }
    }
}
