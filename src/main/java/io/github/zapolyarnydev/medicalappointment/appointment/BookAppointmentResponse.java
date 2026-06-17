package io.github.zapolyarnydev.medicalappointment.appointment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record BookAppointmentResponse(
    boolean available, @NotNull String message, @Nullable Long appointmentId) {

  public static @NotNull BookAppointmentResponse from(@NotNull BookAppointmentResult result) {
    return new BookAppointmentResponse(
        result.available(), result.message(), result.appointmentId());
  }
}
