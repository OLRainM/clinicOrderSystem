package com.clinic.order.doctor.repository;

import com.clinic.order.doctor.dto.FinishVisitRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DoctorWorkspaceRepository {
    private final JdbcTemplate jdbcTemplate;

    public DoctorWorkspaceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Map<String, Object>> profile(Long userId) {
        return jdbcTemplate.query("""
            SELECT dp.user_id, dp.real_name, dp.department_id, dep.name department_name, dp.title, dp.introduction
            FROM doctor_profile dp JOIN department dep ON dp.department_id = dep.id
            WHERE dp.user_id = ?
            """, rs -> rs.next() ? Optional.of(Map.of(
                "userId", rs.getLong("user_id"),
                "realName", rs.getString("real_name"),
                "departmentId", rs.getLong("department_id"),
                "departmentName", rs.getString("department_name"),
                "title", rs.getString("title"),
                "introduction", rs.getString("introduction") == null ? "" : rs.getString("introduction")
        )) : Optional.empty(), userId);
    }

    public List<Map<String, Object>> queue(Long userId, LocalDate date) {
        return jdbcTemplate.queryForList("""
            SELECT ao.order_no, ao.user_id patient_user_id, pp.real_name patient_name,
                   ds.doctor_id, d.name doctor_name, ds.schedule_date, ss.start_time, ss.end_time,
                   CASE WHEN mr.id IS NULL THEN 0 ELSE 1 END completed
            FROM appointment_order ao
            JOIN schedule_slot ss ON ao.slot_id = ss.id
            JOIN doctor_schedule ds ON ss.schedule_id = ds.id
            JOIN doctor d ON ds.doctor_id = d.id
            LEFT JOIN patient_profile pp ON ao.user_id = pp.user_id
            LEFT JOIN medical_record mr ON mr.order_no = ao.order_no
            JOIN doctor_profile dp ON dp.department_id = ds.department_id AND dp.user_id = ?
            WHERE ao.status = 1 AND ds.schedule_date = ?
            ORDER BY completed, ss.start_time, ao.id
            """, userId, date);
    }

    public Optional<Map<String, Object>> orderDetail(Long doctorUserId, String orderNo) {
        return jdbcTemplate.query("""
            SELECT ao.order_no, ao.user_id patient_user_id, pp.real_name patient_name, pp.gender, pp.birthday,
                   ds.doctor_id, d.name doctor_name, dep.name department_name, ds.schedule_date, ss.start_time, ss.end_time
            FROM appointment_order ao
            JOIN schedule_slot ss ON ao.slot_id = ss.id
            JOIN doctor_schedule ds ON ss.schedule_id = ds.id
            JOIN doctor d ON ds.doctor_id = d.id
            JOIN department dep ON ds.department_id = dep.id
            LEFT JOIN patient_profile pp ON ao.user_id = pp.user_id
            JOIN doctor_profile dp ON dp.department_id = ds.department_id AND dp.user_id = ?
            WHERE ao.order_no = ? AND ao.status = 1
            """, rs -> rs.next() ? Optional.of(Map.of(
                "orderNo", rs.getString("order_no"),
                "patientUserId", rs.getLong("patient_user_id"),
                "patientName", rs.getString("patient_name") == null ? "患者" + rs.getLong("patient_user_id") : rs.getString("patient_name"),
                "doctorId", rs.getLong("doctor_id"),
                "doctorName", rs.getString("doctor_name"),
                "departmentName", rs.getString("department_name"),
                "scheduleDate", rs.getDate("schedule_date").toLocalDate().toString(),
                "startTime", rs.getTime("start_time").toLocalTime().toString(),
                "endTime", rs.getTime("end_time").toLocalTime().toString()
        )) : Optional.empty(), doctorUserId, orderNo);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long finishVisit(Long doctorUserId, FinishVisitRequest req) {
        Map<String, Object> order = orderDetail(doctorUserId, req.getOrderNo()).orElseThrow(() -> new IllegalStateException("订单不存在或无权接诊"));
        Integer exists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM medical_record WHERE order_no = ?", Integer.class, req.getOrderNo());
        if (exists != null && exists > 0) throw new IllegalStateException("该订单已完成问诊");
        Long recordId = createRecord(req, order);
        Long prescriptionId = createPrescription(recordId, (Long) order.get("patientUserId"));
        if (req.getItems() != null) {
            for (FinishVisitRequest.Item item : req.getItems()) {
                jdbcTemplate.update("INSERT INTO prescription_item(prescription_id,medicine_name,dosage,usage_instruction) VALUES (?,?,?,?)",
                        prescriptionId, item.getMedicineName(), item.getDosage(), item.getUsageInstruction());
            }
        }
        return recordId;
    }

    private Long createRecord(FinishVisitRequest req, Map<String, Object> order) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO medical_record(order_no,user_id,doctor_id,symptoms,diagnosis)
                VALUES (?,?,?,?,?)
                """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, req.getOrderNo());
            ps.setLong(2, (Long) order.get("patientUserId"));
            ps.setLong(3, (Long) order.get("doctorId"));
            ps.setString(4, req.getSymptoms());
            ps.setString(5, req.getDiagnosis());
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    private Long createPrescription(Long recordId, Long patientUserId) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO prescription(record_id,user_id,status) VALUES (?,?,0)", Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, recordId);
            ps.setLong(2, patientUserId);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }
}
