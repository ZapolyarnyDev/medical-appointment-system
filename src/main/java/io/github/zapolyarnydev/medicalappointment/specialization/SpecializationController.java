package io.github.zapolyarnydev.medicalappointment.specialization;

import io.github.zapolyarnydev.medicalappointment.doctor.DoctorResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SpecializationController {

  private final SpecializationService catalogService;

  @GetMapping("/specializations")
  public @NotNull List<SpecializationResponse> findSpecializations() {
    return catalogService.findSpecializations().stream().map(SpecializationResponse::from).toList();
  }

  @GetMapping("/specializations/{specializationId}/doctors")
  public @NotNull List<DoctorResponse> findDoctorsBySpecialization(
      @PathVariable Long specializationId) {
    return catalogService.findActiveDoctorsBySpecialization(specializationId).stream()
        .map(DoctorResponse::from)
        .toList();
  }
}
