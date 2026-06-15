package com.clinic.order.user.repository;

import com.clinic.order.user.dto.DoctorRegisterRequest;
import com.clinic.order.user.dto.PatientRegisterRequest;
import com.clinic.order.user.model.SysUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long createUser(String phone, String passwordHash, int roleType) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO sys_user(phone,password_hash,role_type,status) VALUES (?,?,?,1)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, phone);
            ps.setString(2, passwordHash);
            ps.setInt(3, roleType);
            return ps;
        }, keyHolder);
        return keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
    }

    public void createPatientProfile(Long userId, PatientRegisterRequest req) {
        jdbcTemplate.update("""
            INSERT INTO patient_profile(user_id,real_name,id_card_no,gender,birthday,emergency_contact)
            VALUES (?,?,?,?,?,?)
            """, userId, req.getRealName(), req.getIdCardNo(), req.getGender(),
                req.getBirthday() == null ? null : Date.valueOf(req.getBirthday()), req.getEmergencyContact());
    }

    public void createDoctorProfile(Long userId, DoctorRegisterRequest req) {
        jdbcTemplate.update("""
            INSERT INTO doctor_profile(user_id,real_name,department_id,title,introduction)
            VALUES (?,?,?,?,?)
            """, userId, req.getRealName(), req.getDepartmentId(), req.getTitle(), req.getIntroduction());
    }

    public Optional<SysUser> findByPhone(String phone) {
        return jdbcTemplate.query("SELECT * FROM sys_user WHERE phone = ?", rs -> rs.next() ? Optional.of(map(rs)) : Optional.empty(), phone);
    }

    public Optional<SysUser> findById(Long id) {
        return jdbcTemplate.query("SELECT * FROM sys_user WHERE id = ?", rs -> rs.next() ? Optional.of(map(rs)) : Optional.empty(), id);
    }

    private SysUser map(java.sql.ResultSet rs) throws java.sql.SQLException {
        SysUser user = new SysUser();
        user.setId(rs.getLong("id"));
        user.setPhone(rs.getString("phone"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRoleType(rs.getInt("role_type"));
        user.setStatus(rs.getInt("status"));
        return user;
    }
}
