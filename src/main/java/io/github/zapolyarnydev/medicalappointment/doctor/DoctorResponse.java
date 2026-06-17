package io.github.zapolyarnydev.medicalappointment.doctor;

import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record DoctorResponse(
    @NotNull Long id,
    @NotNull Long specializationId,
    @NotNull String fullName,
    @Nullable String cabinet,
    boolean active,
    @NotNull LocalDateTime createdAt) {

  public static @NotNull DoctorResponse from(@NotNull Doctor doctor) {
    return new DoctorResponse(
        doctor.id(),
        doctor.specializationId(),
        doctor.fullName(),
        doctor.cabinet(),
        doctor.active(),
        doctor.createdAt());
  }
}
