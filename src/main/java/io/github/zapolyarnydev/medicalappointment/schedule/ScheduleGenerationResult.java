package io.github.zapolyarnydev.medicalappointment.schedule;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ScheduleGenerationResult(
    int createdSlots, int skippedDuplicateSlots, int skippedBreakSlots, @Nullable String error) {

  public static @NotNull ScheduleGenerationResult rejected(@NotNull String error) {
    return new ScheduleGenerationResult(0, 0, 0, error);
  }

  public boolean successful() {
    return error == null;
  }
}
