package io.github.zapolyarnydev.medicalappointment.appointment;

import io.github.zapolyarnydev.medicalappointment.identity.CurrentUserService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentPatientAppointmentService {

  private final AppointmentBookingService appointmentBookingService;
  private final CurrentUserService currentUserService;

  public @NotNull BookAppointmentResult book(
      Principal principal, @Nullable Long doctorId, @Nullable Long slotId) {
    return currentUserService
        .patientAccount(principal)
        .map(
            patientAccount ->
                appointmentBookingService.book(
                    new BookAppointmentCommand(
                        doctorId, patientAccount.patientId(), slotId, AppointmentSource.ONLINE)))
        .orElseGet(
            () -> BookAppointmentResult.rejected("Профиль пациента не привязан к учетной записи"));
  }
}
