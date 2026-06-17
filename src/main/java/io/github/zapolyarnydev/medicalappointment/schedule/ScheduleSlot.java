package io.github.zapolyarnydev.medicalappointment.schedule;

import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;

public record ScheduleSlot(
    @NotNull Long id,
    @NotNull Long doctorId,
    @NotNull LocalDateTime startTime,
    @NotNull LocalDateTime endTime,
    @NotNull SlotStatus status) {}
