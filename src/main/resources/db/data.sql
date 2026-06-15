USE clinic_order;

INSERT INTO department(id, name) VALUES
(1, '内科'), (2, '儿科'), (3, '口腔科');

INSERT INTO sys_user(id, phone, password_hash, role_type, status) VALUES
(1001, '18800000001', '$2a$10$cAhaDTF5eURG0YapZUK5n.CoNvAgXUOyK5SRqX.TdQn2fEVZKoL76', 1, 1),
(2001, '18800000002', '$2a$10$ihkcvmpKvIqLK4kcSOAPcu9tw0h1R3/6acTSzbDkBfHdZCRHqjAIi', 2, 1),
(9001, '18800000003', '$2a$10$pjHUBL9hL5.c32/k3H9dF.7GexRL3ktC0oMpzSQnR2/JbB8ueDAz.', 3, 1);

INSERT INTO patient_profile(user_id, real_name, id_card_no, gender, birthday, emergency_contact) VALUES
(1001, '演示患者', '110101199001010011', 1, '1990-01-01', '18800000009');

INSERT INTO doctor_profile(user_id, real_name, department_id, title, introduction) VALUES
(2001, '演示医生', 1, '主任医师', '擅长内科常见病诊疗');


INSERT INTO doctor(id, department_id, name, title) VALUES
(1, 1, '张医生', '主任医师'),
(2, 1, '李医生', '副主任医师'),
(3, 2, '王医生', '主治医师'),
(4, 3, '赵医生', '主治医师');

INSERT INTO doctor_schedule(id, doctor_id, department_id, schedule_date, period) VALUES
(1, 1, 1, CURDATE(), 'AM'),
(2, 2, 1, CURDATE(), 'PM'),
(3, 3, 2, CURDATE(), 'AM'),
(4, 4, 3, CURDATE(), 'PM');

INSERT INTO schedule_slot(id, schedule_id, start_time, end_time, total_quota, available_quota, version) VALUES
(1, 1, '08:00:00', '08:30:00', 10, 9, 0),
(2, 1, '08:30:00', '09:00:00', 10, 10, 0),
(3, 2, '14:00:00', '14:30:00', 8, 8, 0),
(4, 2, '14:30:00', '15:00:00', 8, 8, 0),
(5, 3, '09:00:00', '09:30:00', 6, 6, 0),
(6, 4, '15:00:00', '15:30:00', 5, 5, 0);

INSERT INTO appointment_order(id, order_no, user_id, slot_id, status, lock_expire_time, paid_at) VALUES
(1, 'AP202601010001', 1001, 1, 1, DATE_ADD(NOW(), INTERVAL 10 MINUTE), NOW());

INSERT INTO medical_record(id, order_no, user_id, doctor_id, symptoms, diagnosis) VALUES
(1, 'AP202601010001', 1001, 1, '咳嗽、咽痛三天', '上呼吸道感染');

INSERT INTO prescription(id, record_id, user_id, status) VALUES
(1, 1, 1001, 0);

INSERT INTO prescription_item(prescription_id, medicine_name, dosage, usage_instruction) VALUES
(1, '阿莫西林胶囊', '0.5g', '口服，一日三次'),
(1, '复方甘草片', '2片', '口服，一日三次');


