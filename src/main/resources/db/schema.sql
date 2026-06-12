CREATE DATABASE IF NOT EXISTS clinic_order DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE clinic_order;

DROP TABLE IF EXISTS appointment;
DROP TABLE IF EXISTS schedule_slot;
DROP TABLE IF EXISTS doctor_schedule;
DROP TABLE IF EXISTS medical_record;
DROP TABLE IF EXISTS doctor;
DROP TABLE IF EXISTS department;

CREATE TABLE department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL
);

CREATE TABLE doctor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    department_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    title VARCHAR(64),
    CONSTRAINT fk_doctor_department FOREIGN KEY (department_id) REFERENCES department(id)
);

CREATE TABLE doctor_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    doctor_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    schedule_date DATE NOT NULL,
    period VARCHAR(16) NOT NULL COMMENT 'AM/PM',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_doctor_date_period (doctor_id, schedule_date, period),
    KEY idx_schedule_department_date (department_id, schedule_date),
    CONSTRAINT fk_schedule_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(id),
    CONSTRAINT fk_schedule_department FOREIGN KEY (department_id) REFERENCES department(id)
);

CREATE TABLE schedule_slot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    schedule_id BIGINT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    total_quota INT NOT NULL,
    available_quota INT NOT NULL,
    version INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_schedule_time (schedule_id, start_time, end_time),
    CONSTRAINT fk_slot_schedule FOREIGN KEY (schedule_id) REFERENCES doctor_schedule(id),
    CONSTRAINT ck_quota CHECK (available_quota >= 0 AND available_quota <= total_quota)
);

CREATE TABLE appointment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL COMMENT 'PENDING_PAY/PAID/CLOSED',
    lock_expire_time DATETIME NOT NULL,
    paid_at DATETIME NULL,
    closed_at DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_status (user_id, status),
    KEY idx_slot_status (slot_id, status),
    CONSTRAINT fk_appointment_slot FOREIGN KEY (slot_id) REFERENCES schedule_slot(id)
);

CREATE TABLE medical_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    appointment_id BIGINT,
    encrypted_content TEXT NOT NULL COMMENT 'AES加密病历内容',
    prescription_url VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
