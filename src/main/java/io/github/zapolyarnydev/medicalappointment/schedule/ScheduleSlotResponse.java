package io.github.zapolyarnydev.medicalappointment.schedule;

import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;

public record ScheduleSlotResponse(
    @NotNull Long id,
    @NotNull Long doctorId,
    @NotNull LocalDateTime startTime,
    @NotNull LocalDateTime endTime,
    @NotNull SlotStatus status) {

  public static @NotNull ScheduleSlotResponse from(@NotNull ScheduleSlot slot) {
    return new ScheduleSlotResponse(
        slot.id(), slot.doctorId(), slot.startTime(), slot.endTime(), slot.status());
  }
}
