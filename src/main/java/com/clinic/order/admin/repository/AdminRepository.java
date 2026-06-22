package com.clinic.order.admin.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Repository
public class AdminRepository {
    private final JdbcTemplate jdbcTemplate;

    public AdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> departments() {
        return jdbcTemplate.queryForList("SELECT id,name FROM department ORDER BY id");
    }

    public List<Map<String, Object>> doctors() {
        return jdbcTemplate.queryForList("""
            SELECT d.id,d.department_id,d.name,d.title,dep.name department_name
            FROM doctor d JOIN department dep ON d.department_id = dep.id
            ORDER BY d.id DESC
            """);
    }

    public int createDoctor(Long departmentId, String name, String title) {
        return jdbcTemplate.update("INSERT INTO doctor(department_id,name,title) VALUES (?,?,?)", departmentId, name, title);
    }

    public int updateDoctor(Long id, Long departmentId, String name, String title) {
        return jdbcTemplate.update("UPDATE doctor SET department_id=?, name=?, title=? WHERE id=?", departmentId, name, title, id);
    }

    public int deleteDoctor(Long id) {
        return jdbcTemplate.update("DELETE FROM doctor WHERE id=?", id);
    }

    public List<Map<String, Object>> schedules(LocalDate date) {
        return jdbcTemplate.queryForList("""
            SELECT ds.id, ds.doctor_id, d.name doctor_name, ds.department_id, dep.name department_name,
                   ds.schedule_date, ds.period,
                   COUNT(ss.id) slot_count, COALESCE(SUM(ss.available_quota),0) available_quota
            FROM doctor_schedule ds
            JOIN doctor d ON ds.doctor_id=d.id
            JOIN department dep ON ds.department_id=dep.id
            LEFT JOIN schedule_slot ss ON ss.schedule_id=ds.id
            WHERE (? IS NULL OR ds.schedule_date=?)
            GROUP BY ds.id,ds.doctor_id,d.name,ds.department_id,dep.name,ds.schedule_date,ds.period
            ORDER BY ds.schedule_date DESC, ds.period, d.name
            """, date, date);
    }

    public Long createSchedule(Long doctorId, Long departmentId, LocalDate date, String period) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO doctor_schedule(doctor_id,department_id,schedule_date,period) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, doctorId);
            ps.setLong(2, departmentId);
            ps.setObject(3, date);
            ps.setString(4, period);
            return ps;
        }, keyHolder);
        return keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
    }

    public int updateSchedule(Long id, Long doctorId, Long departmentId, LocalDate date, String period) {
        return jdbcTemplate.update("UPDATE doctor_schedule SET doctor_id=?, department_id=?, schedule_date=?, period=? WHERE id=?",
                doctorId, departmentId, date, period, id);
    }

    public int deleteSchedule(Long id) {
        jdbcTemplate.update("DELETE FROM schedule_slot WHERE schedule_id=?", id);
        return jdbcTemplate.update("DELETE FROM doctor_schedule WHERE id=?", id);
    }

    public int createSlot(Long scheduleId, LocalTime startTime, LocalTime endTime, Integer totalQuota) {
        return jdbcTemplate.update("""
            INSERT INTO schedule_slot(schedule_id,start_time,end_time,total_quota,available_quota,version)
            VALUES (?,?,?,?,?,0)
            """, scheduleId, Time.valueOf(startTime), Time.valueOf(endTime), totalQuota, totalQuota);
    }

    public List<Map<String, Object>> scheduleSlots(Long scheduleId) {
        return jdbcTemplate.queryForList("""
            SELECT id, schedule_id, start_time, end_time, total_quota, available_quota
            FROM schedule_slot
            WHERE schedule_id = ?
            ORDER BY start_time
            """, scheduleId);
    }

    public int updateSlotQuota(Long slotId, Integer totalQuota) {
        return jdbcTemplate.update("""
            UPDATE schedule_slot
            SET total_quota = ?, available_quota = LEAST(available_quota, ?)
            WHERE id = ?
            """, totalQuota, totalQuota, slotId);
    }

    public int deleteSlot(Long slotId) {
        return jdbcTemplate.update("DELETE FROM schedule_slot WHERE id=?", slotId);
    }
}
