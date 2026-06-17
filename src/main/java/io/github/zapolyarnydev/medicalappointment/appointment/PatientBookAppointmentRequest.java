package io.github.zapolyarnydev.medicalappointment.appointment;

import jakarta.validation.constraints.NotNull;

public record PatientBookAppointmentRequest(
    @NotNull(message = "Не указан врач") Long doctorId,
    @NotNull(message = "Не указан временной слот") Long slotId) {}
