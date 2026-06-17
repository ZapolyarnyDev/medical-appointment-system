package io.github.zapolyarnydev.medicalappointment.schedule;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleService {

  private final ScheduleSlotRepository scheduleSlotRepository;
  private final Clock clock;

  public @NotNull List<ScheduleSlot> findAvailableFutureSlotsByDoctor(@NotNull Long doctorId) {
    return scheduleSlotRepository.findAvailableFutureByDoctorId(doctorId, LocalDateTime.now(clock));
  }

  public @NotNull List<ScheduleSlot> findSlotsByDoctor(@NotNull Long doctorId) {
    return scheduleSlotRepository.findByDoctorId(doctorId);
  }

  public @NotNull ScheduleSlot createSlot(
      @NotNull Long doctorId, @NotNull LocalDateTime startTime, int durationMinutes) {
    return scheduleSlotRepository.create(
        doctorId, startTime, startTime.plusMinutes(durationMinutes), SlotStatus.AVAILABLE);
  }

  public int generateSlots(
      @NotNull Long doctorId,
      @NotNull LocalDate date,
      @NotNull LocalTime workStart,
      @NotNull LocalTime workEnd,
      int durationMinutes,
      LocalTime breakStart,
      LocalTime breakEnd) {
    if (!workStart.isBefore(workEnd) || durationMinutes < 5) {
      return 0;
    }

    int createdSlots = 0;
    LocalDateTime cursor = LocalDateTime.of(date, workStart);
    LocalDateTime end = LocalDateTime.of(date, workEnd);

    while (!cursor.plusMinutes(durationMinutes).isAfter(end)) {
      LocalDateTime slotEnd = cursor.plusMinutes(durationMinutes);
      if (!overlapsBreak(cursor.toLocalTime(), slotEnd.toLocalTime(), breakStart, breakEnd)) {
        createdSlots +=
            scheduleSlotRepository.createIfAbsent(doctorId, cursor, slotEnd, SlotStatus.AVAILABLE);
      }
      cursor = slotEnd;
    }

    return createdSlots;
  }

  private boolean overlapsBreak(
      @NotNull LocalTime slotStart,
      @NotNull LocalTime slotEnd,
      LocalTime breakStart,
      LocalTime breakEnd) {
    return breakStart != null
        && breakEnd != null
        && breakStart.isBefore(breakEnd)
        && slotStart.isBefore(breakEnd)
        && slotEnd.isAfter(breakStart);
  }
}
