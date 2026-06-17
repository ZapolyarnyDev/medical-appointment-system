package io.github.zapolyarnydev.medicalappointment.appointment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Appointment booking and lookup operations")
public class AppointmentController {

  private final AppointmentBookingService appointmentBookingService;
  private final AppointmentQueryService appointmentQueryService;

  @Operation(
      summary = "Book an appointment",
      description =
          "Validates doctor, patient, and slot availability, then creates an appointment and marks the slot as booked.")
  @ApiResponse(
      responseCode = "201",
      description = "Appointment created",
      content = @Content(schema = @Schema(implementation = BookAppointmentResponse.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Booking rejected",
      content = @Content(schema = @Schema(implementation = BookAppointmentResponse.class)))
  @PostMapping("/book")
  public @NotNull ResponseEntity<BookAppointmentResponse> book(
      @RequestBody BookAppointmentRequest request) {
    BookAppointmentResponse response =
        BookAppointmentResponse.from(
            appointmentBookingService.book(
                new BookAppointmentCommand(
                    request.doctorId(), request.patientId(), request.slotId(), request.source())));

    return ResponseEntity.status(response.available() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST)
        .body(response);
  }

  @Operation(
      summary = "Find patient appointments",
      description = "Returns appointments of a patient.")
  @ApiResponse(responseCode = "200", description = "Patient appointments returned")
  @GetMapping("/patients/{patientId}")
  public @NotNull List<AppointmentResponse> findByPatientId(@PathVariable Long patientId) {
    return appointmentQueryService.findByPatientId(patientId).stream()
        .map(AppointmentResponse::from)
        .toList();
  }
}
