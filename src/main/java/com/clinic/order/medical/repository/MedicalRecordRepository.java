package com.clinic.order.medical.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class MedicalRecordRepository {
    private final JdbcTemplate jdbcTemplate;

    public MedicalRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Map<String, Object>> findOwnerRecord(Long recordId, Long userId) {
        String sql = """
            SELECT mr.*, d.name doctor_name
            FROM medical_record mr
            JOIN doctor d ON mr.doctor_id = d.id
            WHERE mr.id = ? AND mr.user_id = ?
            """;
        return jdbcTemplate.query(sql, rs -> rs.next() ? Optional.of(Map.of(
                "id", rs.getLong("id"),
                "orderNo", rs.getString("order_no"),
                "doctorName", rs.getString("doctor_name"),
                "symptoms", rs.getString("symptoms"),
                "diagnosis", rs.getString("diagnosis"),
                "createdAt", rs.getTimestamp("created_at").toLocalDateTime().toString())) : Optional.empty(), recordId, userId);
    }
}
