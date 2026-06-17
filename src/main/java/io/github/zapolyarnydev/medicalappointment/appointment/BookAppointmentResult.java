package io.github.zapolyarnydev.medicalappointment.appointment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record BookAppointmentResult(
    boolean available, @NotNull String message, @Nullable Long appointmentId) {

  public static @NotNull BookAppointmentResult rejected(@NotNull String message) {
    return new BookAppointmentResult(false, message, null);
  }

  public static @NotNull BookAppointmentResult created(@NotNull Long appointmentId) {
    return new BookAppointmentResult(true, "Запись успешно создана", appointmentId);
  }
}
