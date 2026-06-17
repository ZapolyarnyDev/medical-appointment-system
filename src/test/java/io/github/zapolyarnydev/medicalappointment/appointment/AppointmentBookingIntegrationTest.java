package io.github.zapolyarnydev.medicalappointment.appointment;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.zapolyarnydev.medicalappointment.doctor.Doctor;
import io.github.zapolyarnydev.medicalappointment.doctor.DoctorRepository;
import io.github.zapolyarnydev.medicalappointment.patient.Patient;
import io.github.zapolyarnydev.medicalappointment.patient.PatientRepository;
import io.github.zapolyarnydev.medicalappointment.schedule.ScheduleSlot;
import io.github.zapolyarnydev.medicalappointment.schedule.ScheduleSlotRepository;
import io.github.zapolyarnydev.medicalappointment.schedule.SlotStatus;
import io.github.zapolyarnydev.medicalappointment.shared.PostgresIntegrationTest;
import io.github.zapolyarnydev.medicalappointment.specialization.Specialization;
import io.github.zapolyarnydev.medicalappointment.specialization.SpecializationRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AppointmentBookingIntegrationTest extends PostgresIntegrationTest {

  @Autowired private AppointmentBookingService appointmentBookingService;
  @Autowired private AppointmentRepository appointmentRepository;
  @Autowired private DoctorRepository doctorRepository;
  @Autowired private PatientRepository patientRepository;
  @Autowired private ScheduleSlotRepository scheduleSlotRepository;
  @Autowired private SpecializationRepository specializationRepository;

  @Test
  void booksAvailableFutureSlot() {
    TestData data = createBookingData(SlotStatus.AVAILABLE, LocalDateTime.now().plusDays(1));

    BookAppointmentResult result =
        appointmentBookingService.book(
            new BookAppointmentCommand(
                data.doctor().id(),
                data.patient().id(),
                data.slot().id(),
                AppointmentSource.ONLINE));

    assertThat(result.available()).isTrue();
    assertThat(result.appointmentId()).isNotNull();
    assertThat(scheduleSlotRepository.findById(data.slot().id()))
        .hasValueSatisfying(slot -> assertThat(slot.status()).isEqualTo(SlotStatus.BOOKED));
    assertThat(appointmentRepository.findById(result.appointmentId())).isPresent();
  }

  @Test
  void rejectsMissingPatient() {
    TestData data = createBookingData(SlotStatus.AVAILABLE, LocalDateTime.now().plusDays(1));

    BookAppointmentResult result =
        appointmentBookingService.book(
            new BookAppointmentCommand(
                data.doctor().id(), 999L, data.slot().id(), AppointmentSource.ONLINE));

    assertThat(result).isEqualTo(BookAppointmentResult.rejected("Пациент не найден"));
  }

  @Test
  void rejectsMissingDoctor() {
    TestData data = createBookingData(SlotStatus.AVAILABLE, LocalDateTime.now().plusDays(1));

    BookAppointmentResult result =
        appointmentBookingService.book(
            new BookAppointmentCommand(
                999L, data.patient().id(), data.slot().id(), AppointmentSource.ONLINE));

    assertThat(result).isEqualTo(BookAppointmentResult.rejected("Врач не найден"));
  }

  @Test
  void rejectsMissingSlotId() {
    TestData data = createBookingData(SlotStatus.AVAILABLE, LocalDateTime.now().plusDays(1));

    BookAppointmentResult result =
        appointmentBookingService.book(
            new BookAppointmentCommand(
                data.doctor().id(), data.patient().id(), null, AppointmentSource.ONLINE));

    assertThat(result).isEqualTo(BookAppointmentResult.rejected("Не указан временной слот"));
  }

  @Test
  void rejectsBookedSlot() {
    TestData data = createBookingData(SlotStatus.BOOKED, LocalDateTime.now().plusDays(1));

    BookAppointmentResult result =
        appointmentBookingService.book(
            new BookAppointmentCommand(
                data.doctor().id(),
                data.patient().id(),
                data.slot().id(),
                AppointmentSource.ONLINE));

    assertThat(result).isEqualTo(BookAppointmentResult.rejected("Слот занят"));
  }

  @Test
  void rejectsPastSlot() {
    TestData data = createBookingData(SlotStatus.AVAILABLE, LocalDateTime.now().minusDays(1));

    BookAppointmentResult result =
        appointmentBookingService.book(
            new BookAppointmentCommand(
                data.doctor().id(),
                data.patient().id(),
                data.slot().id(),
                AppointmentSource.ONLINE));

    assertThat(result).isEqualTo(BookAppointmentResult.rejected("Слот недоступен для записи"));
  }

  @Test
  void rejectsSlotOfAnotherDoctor() {
    TestData data = createBookingData(SlotStatus.AVAILABLE, LocalDateTime.now().plusDays(1));
    Doctor anotherDoctor =
        doctorRepository.create(data.specialization().id(), "Другой врач", "404");

    BookAppointmentResult result =
        appointmentBookingService.book(
            new BookAppointmentCommand(
                anotherDoctor.id(),
                data.patient().id(),
                data.slot().id(),
                AppointmentSource.ONLINE));

    assertThat(result)
        .isEqualTo(BookAppointmentResult.rejected("Слот не принадлежит выбранному врачу"));
  }

  @Test
  void rejectsRepeatedBooking() {
    TestData data = createBookingData(SlotStatus.AVAILABLE, LocalDateTime.now().plusDays(1));
    BookAppointmentCommand command =
        new BookAppointmentCommand(
            data.doctor().id(), data.patient().id(), data.slot().id(), AppointmentSource.ONLINE);

    BookAppointmentResult first = appointmentBookingService.book(command);
    BookAppointmentResult second = appointmentBookingService.book(command);

    assertThat(first.available()).isTrue();
    assertThat(second).isEqualTo(BookAppointmentResult.rejected("Слот занят"));
  }

  private TestData createBookingData(SlotStatus slotStatus, LocalDateTime startTime) {
    Specialization specialization = specializationRepository.create("Терапия", null);
    Doctor doctor = doctorRepository.create(specialization.id(), "Иванов Иван Иванович", "101");
    Patient patient =
        patientRepository.create(
            "Петров Петр Петрович", LocalDate.of(1990, 1, 1), "79990000000", "POLICY-1");
    ScheduleSlot slot =
        scheduleSlotRepository.create(
            doctor.id(), startTime, startTime.plusMinutes(30), slotStatus);

    return new TestData(specialization, doctor, patient, slot);
  }

  private static record TestData(
      Specialization specialization, Doctor doctor, Patient patient, ScheduleSlot slot) {}
}
