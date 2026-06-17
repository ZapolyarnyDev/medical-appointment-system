package io.github.zapolyarnydev.medicalappointment.appointment;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

  private final AppointmentBookingService appointmentBookingService;

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
}
