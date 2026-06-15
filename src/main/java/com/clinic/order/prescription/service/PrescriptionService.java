package com.clinic.order.prescription.service;

import com.clinic.order.common.security.SecurityAuditService;
import com.clinic.order.prescription.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PrescriptionService {
    private final PrescriptionRepository repository;
    private final SecurityAuditService auditService;

    public PrescriptionService(PrescriptionRepository repository, SecurityAuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Optional<Map<String, Object>> getPrescriptionData(Long prescriptionId, Long userId) {
        Optional<Map<String, Object>> prescription = repository.findPrescription(prescriptionId, userId);
        auditService.record(userId, "PRESCRIPTION", String.valueOf(prescriptionId), "DOWNLOAD_PDF",
                prescription.isPresent() ? "ALLOW" : "DENY", prescription.isPresent() ? "OWNER_MATCH" : "NOT_FOUND_OR_NOT_OWNER");
        if (prescription.isEmpty()) return Optional.empty();
        Map<String, Object> data = new HashMap<>(prescription.get());
        data.put("patientName", "患者" + userId);
        data.put("items", repository.findItems(prescriptionId));
        return Optional.of(data);
    }
}
