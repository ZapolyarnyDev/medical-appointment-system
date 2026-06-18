package io.github.zapolyarnydev.medicalappointment.appointment;

import io.github.zapolyarnydev.medicalappointment.schedule.ScheduleSlotRepository;
import io.github.zapolyarnydev.medicalappointment.schedule.SlotStatus;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppointmentManagementService {

  private final AppointmentBookingService appointmentBookingService;
  private final AppointmentRepository appointmentRepository;
  private final ScheduleSlotRepository scheduleSlotRepository;
  private final Clock clock;

  @Transactional
  public @NotNull BookAppointmentResult cancelByRegistry(
      @Nullable Long appointmentId, @Nullable String reason) {
    if (appointmentId == null) {
      return BookAppointmentResult.rejected("Не указана запись");
    }

    return appointmentRepository
        .findById(appointmentId)
        .map(appointment -> cancelExistingAppointment(appointment, reason))
        .orElseGet(() -> BookAppointmentResult.rejected("Запись не найдена"));
  }

  @Transactional
  public @NotNull BookAppointmentResult rescheduleByRegistry(
      @Nullable Long appointmentId, @Nullable Long doctorId, @Nullable Long slotId) {
    if (appointmentId == null) {
      return BookAppointmentResult.rejected("Не указана запись");
    }

    var appointment = appointmentRepository.findById(appointmentId);
    if (appointment.isEmpty()) {
      return BookAppointmentResult.rejected("Запись не найдена");
    }
    if (appointment.get().status() != AppointmentStatus.CREATED) {
      return BookAppointmentResult.rejected("Перенести можно только активную запись");
    }
    if (appointment.get().slotId().equals(slotId)) {
      return BookAppointmentResult.rejected("Выберите другое время");
    }

    BookAppointmentResult bookingResult =
        appointmentBookingService.book(
            new BookAppointmentCommand(
                doctorId, appointment.get().patientId(), slotId, AppointmentSource.REGISTRY));
    if (!bookingResult.available()) {
      return bookingResult;
    }

    cancelExistingAppointment(appointment.get(), "Перенесено регистратурой");
    return BookAppointmentResult.created(bookingResult.appointmentId());
  }

  private @NotNull BookAppointmentResult cancelExistingAppointment(
      @NotNull Appointment appointment, @Nullable String reason) {
    if (appointment.status() != AppointmentStatus.CREATED) {
      return BookAppointmentResult.rejected("Эту запись нельзя отменить");
    }

    var slot = scheduleSlotRepository.findById(appointment.slotId());
    if (slot.isEmpty() || !slot.get().startTime().isAfter(LocalDateTime.now(clock))) {
      return BookAppointmentResult.rejected("Прошедшую запись нельзя отменить");
    }

    appointmentRepository.updateStatus(
        appointment.id(), AppointmentStatus.CANCELLED, normalizeReason(reason));
    scheduleSlotRepository.updateStatus(appointment.slotId(), SlotStatus.AVAILABLE);
    return BookAppointmentResult.cancelled(appointment.id());
  }

  private String normalizeReason(String reason) {
    return reason == null || reason.isBlank() ? "Отменено регистратурой" : reason.trim();
  }
}
