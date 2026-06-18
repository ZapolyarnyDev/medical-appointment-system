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
    if (!startTime.isAfter(LocalDateTime.now(clock)) || durationMinutes < 5) {
      throw new IllegalArgumentException("Время приема должно быть в будущем");
    }
    return scheduleSlotRepository.create(
        doctorId, startTime, startTime.plusMinutes(durationMinutes), SlotStatus.AVAILABLE);
  }

  public @NotNull ScheduleGenerationResult generateSlots(
      @NotNull Long doctorId,
      @NotNull LocalDate date,
      @NotNull LocalTime workStart,
      @NotNull LocalTime workEnd,
      int durationMinutes,
      LocalTime breakStart,
      LocalTime breakEnd) {
    if (date.isBefore(LocalDate.now(clock))) {
      return ScheduleGenerationResult.rejected("Нельзя создать расписание в прошлом");
    }
    if (!workStart.isBefore(workEnd) || durationMinutes < 5) {
      return ScheduleGenerationResult.rejected("Проверьте рабочее время и длительность приема");
    }

    int createdSlots = 0;
    int skippedDuplicateSlots = 0;
    int skippedBreakSlots = 0;
    LocalDateTime cursor = LocalDateTime.of(date, workStart);
    LocalDateTime end = LocalDateTime.of(date, workEnd);

    while (!cursor.plusMinutes(durationMinutes).isAfter(end)) {
      LocalDateTime slotEnd = cursor.plusMinutes(durationMinutes);
      if (overlapsBreak(cursor.toLocalTime(), slotEnd.toLocalTime(), breakStart, breakEnd)) {
        skippedBreakSlots++;
      } else {
        int created =
            scheduleSlotRepository.createIfAbsent(doctorId, cursor, slotEnd, SlotStatus.AVAILABLE);
        createdSlots += created;
        if (created == 0) {
          skippedDuplicateSlots++;
        }
      }
      cursor = slotEnd;
    }

    return new ScheduleGenerationResult(
        createdSlots, skippedDuplicateSlots, skippedBreakSlots, null);
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
