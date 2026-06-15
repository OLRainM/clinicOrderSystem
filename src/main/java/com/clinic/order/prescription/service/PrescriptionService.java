package com.clinic.order.prescription.service;

import com.clinic.order.prescription.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PrescriptionService {
    private final PrescriptionRepository repository;

    public PrescriptionService(PrescriptionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<Map<String, Object>> getPrescriptionData(Long prescriptionId, Long userId) {
        Optional<Map<String, Object>> prescription = repository.findPrescription(prescriptionId, userId);
        if (prescription.isEmpty()) return Optional.empty();
        Map<String, Object> data = new HashMap<>(prescription.get());
        data.put("patientName", "患者" + userId);
        data.put("items", repository.findItems(prescriptionId));
        return Optional.of(data);
    }
}
