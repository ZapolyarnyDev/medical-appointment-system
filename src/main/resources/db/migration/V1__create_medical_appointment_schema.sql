CREATE TABLE specializations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp
);

CREATE TABLE doctors (
    id BIGSERIAL PRIMARY KEY,
    specialization_id BIGINT NOT NULL REFERENCES specializations (id),
    full_name VARCHAR(255) NOT NULL,
    cabinet VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp
);

CREATE INDEX idx_doctors_specialization_id ON doctors (specialization_id);

CREATE TABLE patients (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    birth_date DATE NOT NULL,
    phone VARCHAR(30) NOT NULL UNIQUE,
    policy_number VARCHAR(50) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp
);

CREATE TABLE schedule_slots (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL REFERENCES doctors (id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT uq_schedule_slots_doctor_start_time UNIQUE (doctor_id, start_time),
    CONSTRAINT chk_schedule_slots_time_range CHECK (start_time < end_time),
    CONSTRAINT chk_schedule_slots_status CHECK (status IN ('AVAILABLE', 'BOOKED'))
);

CREATE INDEX idx_schedule_slots_doctor_start_time ON schedule_slots (doctor_id, start_time);
CREATE INDEX idx_schedule_slots_status_start_time ON schedule_slots (status, start_time);

CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients (id),
    slot_id BIGINT NOT NULL UNIQUE REFERENCES schedule_slots (id),
    status VARCHAR(30) NOT NULL,
    source VARCHAR(30) NOT NULL DEFAULT 'ONLINE',
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
    cancel_reason TEXT,
    CONSTRAINT chk_appointments_status CHECK (status IN ('CREATED', 'CANCELLED', 'COMPLETED')),
    CONSTRAINT chk_appointments_source CHECK (source IN ('ONLINE', 'REGISTRY'))
);

CREATE INDEX idx_appointments_patient_id ON appointments (patient_id);
CREATE INDEX idx_appointments_status_created_at ON appointments (status, created_at);
