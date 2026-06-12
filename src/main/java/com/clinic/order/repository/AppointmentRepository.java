package com.clinic.order.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class AppointmentRepository {
    private final JdbcTemplate jdbcTemplate;

    public AppointmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createPending(Long userId, Long slotId, LocalDateTime expireTime) {
        String sql = "INSERT INTO appointment(user_id, slot_id, status, lock_expire_time) VALUES (?, ?, 'PENDING_PAY', ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setLong(2, slotId);
            ps.setTimestamp(3, Timestamp.valueOf(expireTime));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    public Optional<Long> findPendingId(Long userId, Long slotId) {
        String sql = "SELECT id FROM appointment WHERE user_id = ? AND slot_id = ? AND status = 'PENDING_PAY' LIMIT 1";
        return jdbcTemplate.query(sql, rs -> rs.next() ? Optional.of(rs.getLong("id")) : Optional.empty(), userId, slotId);
    }

    public int markPaid(Long appointmentId, Long userId) {
        String sql = "UPDATE appointment SET status = 'PAID', paid_at = NOW() WHERE id = ? AND user_id = ? AND status = 'PENDING_PAY' AND lock_expire_time > NOW()";
        return jdbcTemplate.update(sql, appointmentId, userId);
    }

    public int closePending(Long appointmentId, Long userId) {
        String sql = "UPDATE appointment SET status = 'CLOSED', closed_at = NOW() WHERE id = ? AND user_id = ? AND status = 'PENDING_PAY'";
        return jdbcTemplate.update(sql, appointmentId, userId);
    }

    public Long findSlotId(Long appointmentId) {
        return jdbcTemplate.queryForObject("SELECT slot_id FROM appointment WHERE id = ?", Long.class, appointmentId);
    }
}
