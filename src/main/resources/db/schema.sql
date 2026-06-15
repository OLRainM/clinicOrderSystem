CREATE DATABASE IF NOT EXISTS clinic_order DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE clinic_order;

DROP TABLE IF EXISTS security_audit_log;

DROP TABLE IF EXISTS prescription_item;
DROP TABLE IF EXISTS prescription;
DROP TABLE IF EXISTS stat_daily_doctor;
DROP TABLE IF EXISTS stat_daily_department;
DROP TABLE IF EXISTS medical_record;
DROP TABLE IF EXISTS appointment_order;
DROP TABLE IF EXISTS schedule_slot;
DROP TABLE IF EXISTS doctor_schedule;
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

CREATE TABLE appointment_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(40) NOT NULL,
    user_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    status TINYINT NOT NULL COMMENT '0待支付 1已支付 2已取消 3已改签',
    original_order_no VARCHAR(40) NULL,
    lock_expire_time DATETIME NOT NULL,
    paid_at DATETIME NULL,
    cancelled_at DATETIME NULL,
    rescheduled_at DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_status (user_id, status),
    KEY idx_slot_status (slot_id, status),
    KEY idx_original_order_no (original_order_no),
    CONSTRAINT fk_order_slot FOREIGN KEY (slot_id) REFERENCES schedule_slot(id)
);

CREATE TABLE medical_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(40) NOT NULL,
    user_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    symptoms TEXT NOT NULL,
    diagnosis TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_record_order_no (order_no),
    CONSTRAINT fk_record_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(id)
);

CREATE TABLE prescription (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0有效 1作废',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_prescription_user (user_id),
    CONSTRAINT fk_prescription_record FOREIGN KEY (record_id) REFERENCES medical_record(id)
);

CREATE TABLE prescription_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    prescription_id BIGINT NOT NULL,
    medicine_name VARCHAR(128) NOT NULL,
    dosage VARCHAR(128) NOT NULL,
    usage_instruction VARCHAR(255) NOT NULL,
    CONSTRAINT fk_item_prescription FOREIGN KEY (prescription_id) REFERENCES prescription(id)
);

CREATE TABLE security_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    resource_type VARCHAR(64) NOT NULL,
    resource_id VARCHAR(64) NOT NULL,
    action VARCHAR(64) NOT NULL,
    result VARCHAR(16) NOT NULL,
    reason VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_audit_user_time (user_id, created_at)
);

CREATE TABLE stat_daily_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stat_date DATE NOT NULL,
    department_id BIGINT NOT NULL,
    department_name VARCHAR(64) NOT NULL,
    visit_count INT NOT NULL DEFAULT 0,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_date_dept (stat_date, department_id)
);

CREATE TABLE stat_daily_doctor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stat_date DATE NOT NULL,
    doctor_id BIGINT NOT NULL,
    doctor_name VARCHAR(64) NOT NULL,
    total_appointments INT NOT NULL DEFAULT 0,
    completed_appointments INT NOT NULL DEFAULT 0,
    reception_rate DECIMAL(5,2) NOT NULL DEFAULT 0,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_date_doc (stat_date, doctor_id)
);
