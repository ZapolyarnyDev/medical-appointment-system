package io.github.zapolyarnydev.medicalappointment.schedule;

import java.time.Clock;
import java.time.LocalDateTime;
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
}
