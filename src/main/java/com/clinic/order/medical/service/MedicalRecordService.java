package com.clinic.order.medical.service;

import com.clinic.order.common.security.SecurityAuditService;
import com.clinic.order.medical.repository.MedicalRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class MedicalRecordService {
    private final MedicalRecordRepository repository;
    private final SecurityAuditService auditService;

    public MedicalRecordService(MedicalRecordRepository repository, SecurityAuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Optional<Map<String, Object>> findOwnerRecord(Long recordId, Long userId) {
        Optional<Map<String, Object>> data = repository.findOwnerRecord(recordId, userId);
        auditService.record(userId, "MEDICAL_RECORD", String.valueOf(recordId), "VIEW",
                data.isPresent() ? "ALLOW" : "DENY", data.isPresent() ? "OWNER_MATCH" : "NOT_FOUND_OR_NOT_OWNER");
        return data;
    }
}
