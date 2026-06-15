package com.clinic.order.stats.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public class StatsRepository {
    private final JdbcTemplate jdbcTemplate;

    public StatsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void aggregateDepartment(LocalDate date) {
        jdbcTemplate.update("""
            INSERT INTO stat_daily_department(stat_date, department_id, department_name, visit_count, updated_at)
            SELECT DATE(ao.paid_at), ds.department_id, dep.name, COUNT(*), NOW()
            FROM appointment_order ao
            JOIN schedule_slot ss ON ao.slot_id = ss.id
            JOIN doctor_schedule ds ON ss.schedule_id = ds.id
            JOIN department dep ON ds.department_id = dep.id
            WHERE ao.status = 1 AND DATE(ao.paid_at) = ?
            GROUP BY DATE(ao.paid_at), ds.department_id, dep.name
            ON DUPLICATE KEY UPDATE visit_count = VALUES(visit_count), updated_at = NOW()
            """, date);
    }

    public void aggregateDoctor(LocalDate date) {
        jdbcTemplate.update("""
            INSERT INTO stat_daily_doctor(stat_date, doctor_id, doctor_name, total_appointments, completed_appointments, reception_rate, updated_at)
            SELECT DATE(ao.paid_at), ds.doctor_id, d.name, COUNT(*), COUNT(mr.id),
                   ROUND(IF(COUNT(*) = 0, 0, COUNT(mr.id) / COUNT(*) * 100), 2), NOW()
            FROM appointment_order ao
            JOIN schedule_slot ss ON ao.slot_id = ss.id
            JOIN doctor_schedule ds ON ss.schedule_id = ds.id
            JOIN doctor d ON ds.doctor_id = d.id
            LEFT JOIN medical_record mr ON mr.order_no = ao.order_no
            WHERE ao.status = 1 AND DATE(ao.paid_at) = ?
            GROUP BY DATE(ao.paid_at), ds.doctor_id, d.name
            ON DUPLICATE KEY UPDATE total_appointments = VALUES(total_appointments),
              completed_appointments = VALUES(completed_appointments), reception_rate = VALUES(reception_rate), updated_at = NOW()
            """, date);
    }

    public List<Map<String, Object>> departmentStats(LocalDate start, LocalDate end) {
        return jdbcTemplate.queryForList("""
            SELECT department_id, department_name, SUM(visit_count) visit_count
            FROM stat_daily_department WHERE stat_date BETWEEN ? AND ?
            GROUP BY department_id, department_name ORDER BY visit_count DESC
            """, start, end);
    }

    public List<Map<String, Object>> doctorStats(LocalDate start, LocalDate end) {
        return jdbcTemplate.queryForList("""
            SELECT doctor_id, doctor_name, SUM(total_appointments) total_appointments,
                   SUM(completed_appointments) completed_appointments,
                   ROUND(IF(SUM(total_appointments)=0,0,SUM(completed_appointments)/SUM(total_appointments)*100),2) reception_rate
            FROM stat_daily_doctor WHERE stat_date BETWEEN ? AND ?
            GROUP BY doctor_id, doctor_name ORDER BY reception_rate DESC
            """, start, end);
    }
}
