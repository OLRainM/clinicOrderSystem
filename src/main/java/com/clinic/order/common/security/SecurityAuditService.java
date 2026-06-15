package com.clinic.order.common.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
public class SecurityAuditService {
    private final JdbcTemplate jdbcTemplate;

    public SecurityAuditService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId, String resourceType, String resourceId, String action, String result, String reason) {
        jdbcTemplate.update("""
            INSERT INTO security_audit_log(user_id, resource_type, resource_id, action, result, reason)
            VALUES (?, ?, ?, ?, ?, ?)
            """, userId, resourceType, resourceId, action, result, reason);
    }
}
