package com.clinic.order.appointment.repository;

import com.clinic.order.appointment.model.AppointmentStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Repository
public class AppointmentOrderRepository {
    private final JdbcTemplate jdbcTemplate;

    public AppointmentOrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long create(String orderNo, Long userId, Long slotId, int status, String originalOrderNo, LocalDateTime expireTime) {
        String sql = """
            INSERT INTO appointment_order(order_no,user_id,slot_id,status,original_order_no,lock_expire_time)
            VALUES (?,?,?,?,?,?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, orderNo);
            ps.setLong(2, userId);
            ps.setLong(3, slotId);
            ps.setInt(4, status);
            ps.setString(5, originalOrderNo);
            ps.setTimestamp(6, Timestamp.valueOf(expireTime));
            return ps;
        }, keyHolder);
        return keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
    }

    public Optional<Map<String, Object>> findOwnerOrder(String orderNo, Long userId) {
        String sql = "SELECT * FROM appointment_order WHERE order_no = ? AND user_id = ?";
        return jdbcTemplate.query(sql, rs -> rs.next() ? Optional.of(Map.of(
                "orderNo", rs.getString("order_no"),
                "slotId", rs.getLong("slot_id"),
                "status", rs.getInt("status"))) : Optional.empty(), orderNo, userId);
    }

    public Optional<Map<String, Object>> findOwnerOrder(Long orderId, Long userId) {
        String sql = "SELECT * FROM appointment_order WHERE id = ? AND user_id = ?";
        return jdbcTemplate.query(sql, rs -> rs.next() ? Optional.of(Map.of(
                "orderNo", rs.getString("order_no"),
                "slotId", rs.getLong("slot_id"),
                "status", rs.getInt("status"))) : Optional.empty(), orderId, userId);
    }

    public int markPaid(Long orderId, Long userId) {
        return jdbcTemplate.update("""
            UPDATE appointment_order SET status = ?, paid_at = NOW(), updated_at = NOW()
            WHERE id = ? AND user_id = ? AND status = ? AND lock_expire_time > NOW()
            """, AppointmentStatus.PAID.getCode(), orderId, userId, AppointmentStatus.PENDING_PAY.getCode());
    }

    public int cancel(Long orderId, Long userId, int expectedStatus) {
        return jdbcTemplate.update("""
            UPDATE appointment_order SET status = ?, cancelled_at = NOW(), updated_at = NOW()
            WHERE id = ? AND user_id = ? AND status = ?
            """, AppointmentStatus.CANCELLED.getCode(), orderId, userId, expectedStatus);
    }

    public int markPaid(String orderNo, Long userId) {
        return jdbcTemplate.update("""
            UPDATE appointment_order SET status = ?, paid_at = NOW(), updated_at = NOW()
            WHERE order_no = ? AND user_id = ? AND status = ? AND lock_expire_time > NOW()
            """, AppointmentStatus.PAID.getCode(), orderNo, userId, AppointmentStatus.PENDING_PAY.getCode());
    }


    public int markRescheduled(String orderNo, Long userId) {
        return jdbcTemplate.update("""
            UPDATE appointment_order SET status = ?, rescheduled_at = NOW(), updated_at = NOW()
            WHERE order_no = ? AND user_id = ? AND status = ?
            """, AppointmentStatus.RESCHEDULED.getCode(), orderNo, userId, AppointmentStatus.PAID.getCode());
    }
}
