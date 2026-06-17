package io.github.zapolyarnydev.medicalappointment.appointment;

import org.jetbrains.annotations.Nullable;

public record BookAppointmentRequest(
    @Nullable Long doctorId,
    @Nullable Long patientId,
    @Nullable Long slotId,
    @Nullable AppointmentSource source) {}
