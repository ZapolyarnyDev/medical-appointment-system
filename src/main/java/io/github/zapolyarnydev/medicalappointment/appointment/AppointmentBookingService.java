package io.github.zapolyarnydev.medicalappointment.appointment;

import io.github.zapolyarnydev.medicalappointment.doctor.DoctorRepository;
import io.github.zapolyarnydev.medicalappointment.patient.PatientRepository;
import io.github.zapolyarnydev.medicalappointment.schedule.ScheduleSlot;
import io.github.zapolyarnydev.medicalappointment.schedule.ScheduleSlotRepository;
import io.github.zapolyarnydev.medicalappointment.schedule.SlotStatus;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppointmentBookingService {

  private final PatientRepository patientRepository;
  private final DoctorRepository doctorRepository;
  private final ScheduleSlotRepository scheduleSlotRepository;
  private final AppointmentRepository appointmentRepository;
  private final Clock clock;

  @Transactional
  public @NotNull BookAppointmentResult book(@NotNull BookAppointmentCommand command) {
    if (command.doctorId() == null) {
      return BookAppointmentResult.rejected("Не указан врач");
    }
    if (command.patientId() == null) {
      return BookAppointmentResult.rejected("Не указан пациент");
    }
    if (command.slotId() == null) {
      return BookAppointmentResult.rejected("Не указан временной слот");
    }
    if (!patientRepository.existsById(command.patientId())) {
      return BookAppointmentResult.rejected("Пациент не найден");
    }
    if (!doctorRepository.existsById(command.doctorId())) {
      return BookAppointmentResult.rejected("Врач не найден");
    }

    return scheduleSlotRepository
        .findById(command.slotId())
        .map(slot -> bookExistingSlot(command, slot))
        .orElseGet(() -> BookAppointmentResult.rejected("Временной слот не найден"));
  }

  private @NotNull BookAppointmentResult bookExistingSlot(
      @NotNull BookAppointmentCommand command, @NotNull ScheduleSlot slot) {
    if (!slot.doctorId().equals(command.doctorId())) {
      return BookAppointmentResult.rejected("Слот не принадлежит выбранному врачу");
    }
    if (slot.status() != SlotStatus.AVAILABLE) {
      return BookAppointmentResult.rejected("Слот занят");
    }
    if (!slot.startTime().isAfter(LocalDateTime.now(clock))) {
      return BookAppointmentResult.rejected("Слот недоступен для записи");
    }
    if (appointmentRepository.existsBySlotId(slot.id())) {
      return BookAppointmentResult.rejected("Слот занят");
    }

    int updatedRows =
        scheduleSlotRepository.updateStatusIfCurrent(
            slot.id(), SlotStatus.AVAILABLE, SlotStatus.BOOKED);
    if (updatedRows == 0) {
      return BookAppointmentResult.rejected("Слот занят");
    }

    Appointment appointment =
        appointmentRepository.create(
            command.patientId(),
            slot.id(),
            command.source() == null ? AppointmentSource.ONLINE : command.source());

    return BookAppointmentResult.created(appointment.id());
  }
}
