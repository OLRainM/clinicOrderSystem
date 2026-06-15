package com.clinic.order.user.service;

import com.clinic.order.user.dto.*;
import com.clinic.order.user.model.SysUser;
import com.clinic.order.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    public static final int ROLE_PATIENT = 1;
    public static final int ROLE_DOCTOR = 2;
    public static final int ROLE_ADMIN = 3;

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long registerPatient(PatientRegisterRequest req) {
        Long userId = userRepository.createUser(req.getPhone(), passwordEncoder.encode(req.getPassword()), ROLE_PATIENT);
        userRepository.createPatientProfile(userId, req);
        return userId;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long registerDoctor(DoctorRegisterRequest req) {
        Long userId = userRepository.createUser(req.getPhone(), passwordEncoder.encode(req.getPassword()), ROLE_DOCTOR);
        userRepository.createDoctorProfile(userId, req);
        return userId;
    }

    public SysUser login(String phone, String password) {
        SysUser user = userRepository.findByPhone(phone).orElseThrow(() -> new IllegalStateException("手机号或密码错误"));
        if (user.getStatus() == null || user.getStatus() != 1) throw new IllegalStateException("账号已被禁用");
        if (!passwordEncoder.matches(password, user.getPasswordHash())) throw new IllegalStateException("手机号或密码错误");
        return user;
    }

    public SysUser findActiveUser(Long userId) {
        SysUser user = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("用户不存在"));
        if (user.getStatus() == null || user.getStatus() != 1) throw new IllegalStateException("账号已被禁用");
        return user;
    }
}
