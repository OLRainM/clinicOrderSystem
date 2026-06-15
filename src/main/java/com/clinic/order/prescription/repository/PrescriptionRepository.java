package com.clinic.order.prescription.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class PrescriptionRepository {
    private final JdbcTemplate jdbcTemplate;

    public PrescriptionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Map<String, Object>> findPrescription(Long prescriptionId, Long userId) {
        String sql = """
            SELECT p.id prescription_id, mr.order_no, mr.symptoms, mr.diagnosis, d.name doctor_name, p.created_at
            FROM prescription p
            JOIN medical_record mr ON p.record_id = mr.id
            JOIN doctor d ON mr.doctor_id = d.id
            WHERE p.id = ? AND p.user_id = ? AND p.status = 0
            """;
        return jdbcTemplate.query(sql, rs -> rs.next() ? Optional.of(Map.of(
                "prescriptionId", rs.getLong("prescription_id"),
                "orderNo", rs.getString("order_no"),
                "symptoms", rs.getString("symptoms"),
                "diagnosis", rs.getString("diagnosis"),
                "doctorName", rs.getString("doctor_name"),
                "createdAt", rs.getTimestamp("created_at").toLocalDateTime().toString())) : Optional.empty(), prescriptionId, userId);
    }

    public List<Map<String, Object>> findItems(Long prescriptionId) {
        return jdbcTemplate.queryForList("""
            SELECT medicine_name, dosage, usage_instruction
            FROM prescription_item WHERE prescription_id = ? ORDER BY id
            """, prescriptionId);
    }
}
