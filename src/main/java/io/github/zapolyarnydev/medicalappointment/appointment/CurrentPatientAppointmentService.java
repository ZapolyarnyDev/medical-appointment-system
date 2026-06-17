package io.github.zapolyarnydev.medicalappointment.appointment;

import io.github.zapolyarnydev.medicalappointment.identity.CurrentUserService;
import io.github.zapolyarnydev.medicalappointment.schedule.ScheduleSlotRepository;
import io.github.zapolyarnydev.medicalappointment.schedule.SlotStatus;
import java.security.Principal;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentPatientAppointmentService {

  private final AppointmentBookingService appointmentBookingService;
  private final AppointmentRepository appointmentRepository;
  private final ScheduleSlotRepository scheduleSlotRepository;
  private final CurrentUserService currentUserService;
  private final Clock clock;

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

  public @NotNull BookAppointmentResult cancel(Principal principal, @Nullable Long appointmentId) {
    if (appointmentId == null) {
      return BookAppointmentResult.rejected("Не указана запись");
    }

    return currentUserService
        .patientAccount(principal)
        .map(patientAccount -> cancelPatientAppointment(patientAccount.patientId(), appointmentId))
        .orElseGet(
            () -> BookAppointmentResult.rejected("Профиль пациента не привязан к учетной записи"));
  }

  private @NotNull BookAppointmentResult cancelPatientAppointment(
      @NotNull Long patientId, @NotNull Long appointmentId) {
    var appointment = appointmentRepository.findById(appointmentId);
    if (appointment.isEmpty() || !appointment.get().patientId().equals(patientId)) {
      return BookAppointmentResult.rejected("Запись не найдена");
    }
    if (appointment.get().status() != AppointmentStatus.CREATED) {
      return BookAppointmentResult.rejected("Эту запись нельзя отменить");
    }

    var slot = scheduleSlotRepository.findById(appointment.get().slotId());
    if (slot.isEmpty() || !slot.get().startTime().isAfter(LocalDateTime.now(clock))) {
      return BookAppointmentResult.rejected("Прошедшую запись нельзя отменить");
    }

    appointmentRepository.updateStatus(
        appointmentId, AppointmentStatus.CANCELLED, "Отменено пациентом");
    scheduleSlotRepository.updateStatus(appointment.get().slotId(), SlotStatus.AVAILABLE);
    return BookAppointmentResult.cancelled(appointmentId);
  }
}
