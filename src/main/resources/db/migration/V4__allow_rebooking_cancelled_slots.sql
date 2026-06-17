ALTER TABLE appointments DROP CONSTRAINT appointments_slot_id_key;

CREATE UNIQUE INDEX uq_appointments_active_slot
    ON appointments (slot_id)
    WHERE status <> 'CANCELLED';
