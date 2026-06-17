package io.github.zapolyarnydev.medicalappointment.specialization;

import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SpecializationResponse(
    @NotNull Long id,
    @NotNull String name,
    @Nullable String description,
    @NotNull LocalDateTime createdAt) {

  public static @NotNull SpecializationResponse from(@NotNull Specialization specialization) {
    return new SpecializationResponse(
        specialization.id(),
        specialization.name(),
        specialization.description(),
        specialization.createdAt());
  }
}
