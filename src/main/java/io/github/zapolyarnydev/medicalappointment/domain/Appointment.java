package io.github.zapolyarnydev.medicalappointment.domain;

import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Appointment(
    @NotNull Long id,
    @NotNull Long patientId,
    @NotNull Long slotId,
    @NotNull AppointmentStatus status,
    @NotNull AppointmentSource source,
    @NotNull LocalDateTime createdAt,
    @Nullable String cancelReason) {}
