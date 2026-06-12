package com.clinic.order.repository;

import com.clinic.order.model.DoctorSchedule;
import com.clinic.order.model.ScheduleSlot;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

@Repository
public class ScheduleRepository {
    private final JdbcTemplate jdbcTemplate;

    public ScheduleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DoctorSchedule> findSchedules(Long departmentId, LocalDate date) {
        String sql = """
            SELECT ds.*, d.name doctor_name, dep.name department_name
            FROM doctor_schedule ds
            JOIN doctor d ON ds.doctor_id = d.id
            JOIN department dep ON ds.department_id = dep.id
            WHERE (? IS NULL OR ds.department_id = ?) AND ds.schedule_date = ?
            ORDER BY ds.period, d.name
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapSchedule(rs), departmentId, departmentId, date);
    }

    public List<ScheduleSlot> findSlotsByScheduleId(Long scheduleId) {
        String sql = "SELECT * FROM schedule_slot WHERE schedule_id = ? ORDER BY start_time";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapSlot(rs), scheduleId);
    }

    public ScheduleSlot findSlot(Long slotId) {
        String sql = "SELECT * FROM schedule_slot WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapSlot(rs), slotId);
    }

    public int decreaseMysqlQuota(Long slotId) {
        String sql = """
            UPDATE schedule_slot
            SET available_quota = available_quota - 1, version = version + 1
            WHERE id = ? AND available_quota > 0
            """;
        return jdbcTemplate.update(sql, slotId);
    }

    public int increaseMysqlQuota(Long slotId) {
        String sql = """
            UPDATE schedule_slot
            SET available_quota = available_quota + 1, version = version + 1
            WHERE id = ? AND available_quota < total_quota
            """;
        return jdbcTemplate.update(sql, slotId);
    }

    private DoctorSchedule mapSchedule(ResultSet rs) throws java.sql.SQLException {
        DoctorSchedule s = new DoctorSchedule();
        s.setId(rs.getLong("id"));
        s.setDoctorId(rs.getLong("doctor_id"));
        s.setDoctorName(rs.getString("doctor_name"));
        s.setDepartmentId(rs.getLong("department_id"));
        s.setDepartmentName(rs.getString("department_name"));
        s.setScheduleDate(rs.getDate("schedule_date").toLocalDate());
        s.setPeriod(rs.getString("period"));
        return s;
    }

    private ScheduleSlot mapSlot(ResultSet rs) throws java.sql.SQLException {
        ScheduleSlot slot = new ScheduleSlot();
        slot.setId(rs.getLong("id"));
        slot.setScheduleId(rs.getLong("schedule_id"));
        Time start = rs.getTime("start_time");
        Time end = rs.getTime("end_time");
        slot.setStartTime(start == null ? null : start.toLocalTime());
        slot.setEndTime(end == null ? null : end.toLocalTime());
        slot.setTotalQuota(rs.getInt("total_quota"));
        slot.setAvailableQuota(rs.getInt("available_quota"));
        slot.setVersion(rs.getInt("version"));
        return slot;
    }
}
