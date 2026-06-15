package com.clinic.order.medical.service;

import com.clinic.order.medical.repository.MedicalRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class MedicalRecordService {
    private final MedicalRecordRepository repository;

    public MedicalRecordService(MedicalRecordRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<Map<String, Object>> findOwnerRecord(Long recordId, Long userId) {
        return repository.findOwnerRecord(recordId, userId);
    }
}
