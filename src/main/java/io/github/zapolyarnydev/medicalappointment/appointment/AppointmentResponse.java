package io.github.zapolyarnydev.medicalappointment.appointment;

import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record AppointmentResponse(
    @NotNull Long id,
    @NotNull Long patientId,
    @NotNull Long slotId,
    @NotNull AppointmentStatus status,
    @NotNull AppointmentSource source,
    @NotNull LocalDateTime createdAt,
    @Nullable String cancelReason) {

  public static @NotNull AppointmentResponse from(@NotNull Appointment appointment) {
    return new AppointmentResponse(
        appointment.id(),
        appointment.patientId(),
        appointment.slotId(),
        appointment.status(),
        appointment.source(),
        appointment.createdAt(),
        appointment.cancelReason());
  }
}
