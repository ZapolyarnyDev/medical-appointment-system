package io.github.zapolyarnydev.medicalappointment.appointment;

import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record AppointmentDetails(
    @NotNull Long id,
    @NotNull Long patientId,
    @NotNull String patientFullName,
    @NotNull Long slotId,
    @NotNull String doctorFullName,
    @Nullable String cabinet,
    @NotNull LocalDateTime startTime,
    @NotNull LocalDateTime endTime,
    @NotNull AppointmentStatus status,
    @NotNull AppointmentSource source,
    @NotNull LocalDateTime createdAt,
    @Nullable String cancelReason) {}
