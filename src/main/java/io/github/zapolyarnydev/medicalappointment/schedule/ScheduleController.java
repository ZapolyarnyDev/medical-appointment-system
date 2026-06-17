package io.github.zapolyarnydev.medicalappointment.schedule;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Schedule", description = "Doctor schedule operations")
public class ScheduleController {

  private final ScheduleService scheduleService;

  @Operation(
      summary = "Find available future slots",
      description = "Returns only future schedule slots with AVAILABLE status for a doctor.")
  @ApiResponse(responseCode = "200", description = "Available slots returned")
  @GetMapping("/{doctorId}/slots/available")
  public @NotNull List<ScheduleSlotResponse> findAvailableSlots(@PathVariable Long doctorId) {
    return scheduleService.findAvailableFutureSlotsByDoctor(doctorId).stream()
        .map(ScheduleSlotResponse::from)
        .toList();
  }
}
