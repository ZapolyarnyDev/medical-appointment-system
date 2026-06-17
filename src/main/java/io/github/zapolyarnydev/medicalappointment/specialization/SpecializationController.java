package io.github.zapolyarnydev.medicalappointment.specialization;

import io.github.zapolyarnydev.medicalappointment.doctor.DoctorResponse;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Specialization and doctor catalog operations")
public class SpecializationController {

  private final SpecializationService catalogService;

  @Operation(summary = "Find specializations", description = "Returns all medical specializations.")
  @ApiResponse(responseCode = "200", description = "Specializations returned")
  @GetMapping("/specializations")
  public @NotNull List<SpecializationResponse> findSpecializations() {
    return catalogService.findSpecializations().stream().map(SpecializationResponse::from).toList();
  }

  @Operation(
      summary = "Find doctors by specialization",
      description = "Returns active doctors for the selected specialization.")
  @ApiResponse(responseCode = "200", description = "Doctors returned")
  @GetMapping("/specializations/{specializationId}/doctors")
  public @NotNull List<DoctorResponse> findDoctorsBySpecialization(
      @PathVariable Long specializationId) {
    return catalogService.findActiveDoctorsBySpecialization(specializationId).stream()
        .map(DoctorResponse::from)
        .toList();
  }
}
