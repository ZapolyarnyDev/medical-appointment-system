CREATE TABLE patient_accounts (
    id BIGSERIAL PRIMARY KEY,
    keycloak_subject VARCHAR(255) UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    patient_id BIGINT NOT NULL REFERENCES patients (id),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp
);

CREATE INDEX idx_patient_accounts_patient_id ON patient_accounts (patient_id);

CREATE TABLE staff_accounts (
    id BIGSERIAL PRIMARY KEY,
    keycloak_subject VARCHAR(255) UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    doctor_id BIGINT REFERENCES doctors (id),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp,
    CONSTRAINT chk_staff_accounts_role CHECK (role IN ('DOCTOR', 'REGISTRAR', 'CHIEF_DOCTOR')),
    CONSTRAINT chk_staff_accounts_doctor_link CHECK (role <> 'DOCTOR' OR doctor_id IS NOT NULL)
);

CREATE INDEX idx_staff_accounts_doctor_id ON staff_accounts (doctor_id);
