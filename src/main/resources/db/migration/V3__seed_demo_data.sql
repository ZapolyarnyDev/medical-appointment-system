INSERT INTO specializations (name, description)
VALUES
    ('Терапия', 'Первичный прием и общие консультации'),
    ('Кардиология', 'Диагностика и лечение заболеваний сердца'),
    ('Неврология', 'Консультации по заболеваниям нервной системы')
ON CONFLICT (name) DO NOTHING;

INSERT INTO doctors (specialization_id, full_name, cabinet)
VALUES
    ((SELECT id FROM specializations WHERE name = 'Терапия'), 'Иванов Иван Иванович', '101'),
    ((SELECT id FROM specializations WHERE name = 'Кардиология'), 'Петрова Анна Сергеевна', '205'),
    ((SELECT id FROM specializations WHERE name = 'Неврология'), 'Сидоров Петр Алексеевич', '312')
ON CONFLICT DO NOTHING;

INSERT INTO patients (full_name, birth_date, phone, policy_number)
VALUES
    ('Смирнова Мария Павловна', DATE '1992-04-12', '+79990000001', 'DEMO-0001')
ON CONFLICT (phone) DO NOTHING;

INSERT INTO patient_accounts (username, patient_id)
VALUES
    ('patient', (SELECT id FROM patients WHERE phone = '+79990000001'))
ON CONFLICT (username) DO NOTHING;

INSERT INTO staff_accounts (username, role, doctor_id)
VALUES
    ('doctor', 'DOCTOR', (SELECT id FROM doctors WHERE full_name = 'Иванов Иван Иванович')),
    ('registrar', 'REGISTRAR', NULL),
    ('chief-doctor', 'CHIEF_DOCTOR', NULL)
ON CONFLICT (username) DO NOTHING;

INSERT INTO schedule_slots (doctor_id, start_time, end_time, status)
VALUES
    (
        (SELECT id FROM doctors WHERE full_name = 'Иванов Иван Иванович'),
        date_trunc('day', current_timestamp) + INTERVAL '1 day 9 hours',
        date_trunc('day', current_timestamp) + INTERVAL '1 day 9 hours 30 minutes',
        'AVAILABLE'
    ),
    (
        (SELECT id FROM doctors WHERE full_name = 'Иванов Иван Иванович'),
        date_trunc('day', current_timestamp) + INTERVAL '1 day 10 hours',
        date_trunc('day', current_timestamp) + INTERVAL '1 day 10 hours 30 minutes',
        'BOOKED'
    ),
    (
        (SELECT id FROM doctors WHERE full_name = 'Петрова Анна Сергеевна'),
        date_trunc('day', current_timestamp) + INTERVAL '2 days 11 hours',
        date_trunc('day', current_timestamp) + INTERVAL '2 days 11 hours 30 minutes',
        'AVAILABLE'
    ),
    (
        (SELECT id FROM doctors WHERE full_name = 'Сидоров Петр Алексеевич'),
        date_trunc('day', current_timestamp) + INTERVAL '3 days 14 hours',
        date_trunc('day', current_timestamp) + INTERVAL '3 days 14 hours 30 minutes',
        'AVAILABLE'
    )
ON CONFLICT (doctor_id, start_time) DO NOTHING;

INSERT INTO appointments (patient_id, slot_id, status, source)
VALUES
    (
        (SELECT id FROM patients WHERE phone = '+79990000001'),
        (
            SELECT id
            FROM schedule_slots
            WHERE doctor_id = (SELECT id FROM doctors WHERE full_name = 'Иванов Иван Иванович')
                AND start_time = date_trunc('day', current_timestamp) + INTERVAL '1 day 10 hours'
        ),
        'CREATED',
        'ONLINE'
    )
ON CONFLICT (slot_id) DO NOTHING;
