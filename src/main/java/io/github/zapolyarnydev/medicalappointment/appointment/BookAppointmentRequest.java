package io.github.zapolyarnydev.medicalappointment.appointment;

import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;

public record BookAppointmentRequest(
    @NotNull(message = "Не указан врач") Long doctorId,
    @NotNull(message = "Не указан пациент") Long patientId,
    @NotNull(message = "Не указан временной слот") Long slotId,
    @Nullable AppointmentSource source) {}
