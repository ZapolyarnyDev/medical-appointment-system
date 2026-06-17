package io.github.zapolyarnydev.medicalappointment.schedule;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class ScheduleController {

  private final ScheduleService scheduleService;

  @GetMapping("/{doctorId}/slots/available")
  public @NotNull List<ScheduleSlotResponse> findAvailableSlots(@PathVariable Long doctorId) {
    return scheduleService.findAvailableFutureSlotsByDoctor(doctorId).stream()
        .map(ScheduleSlotResponse::from)
        .toList();
  }
}
