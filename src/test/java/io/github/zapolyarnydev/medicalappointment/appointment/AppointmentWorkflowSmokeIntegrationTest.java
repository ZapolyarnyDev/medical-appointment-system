package io.github.zapolyarnydev.medicalappointment.appointment;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.zapolyarnydev.medicalappointment.doctor.Doctor;
import io.github.zapolyarnydev.medicalappointment.doctor.DoctorRepository;
import io.github.zapolyarnydev.medicalappointment.patient.Patient;
import io.github.zapolyarnydev.medicalappointment.patient.PatientRepository;
import io.github.zapolyarnydev.medicalappointment.schedule.ScheduleGenerationResult;
import io.github.zapolyarnydev.medicalappointment.schedule.ScheduleService;
import io.github.zapolyarnydev.medicalappointment.schedule.ScheduleSlot;
import io.github.zapolyarnydev.medicalappointment.schedule.ScheduleSlotRepository;
import io.github.zapolyarnydev.medicalappointment.schedule.SlotStatus;
import io.github.zapolyarnydev.medicalappointment.shared.PostgresIntegrationTest;
import io.github.zapolyarnydev.medicalappointment.specialization.Specialization;
import io.github.zapolyarnydev.medicalappointment.specialization.SpecializationRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AppointmentWorkflowSmokeIntegrationTest extends PostgresIntegrationTest {

  @Autowired private AppointmentBookingService appointmentBookingService;
  @Autowired private AppointmentManagementService appointmentManagementService;
  @Autowired private AppointmentRepository appointmentRepository;
  @Autowired private DoctorRepository doctorRepository;
  @Autowired private PatientRepository patientRepository;
  @Autowired private ScheduleService scheduleService;
  @Autowired private ScheduleSlotRepository scheduleSlotRepository;
  @Autowired private SpecializationRepository specializationRepository;

  @Test
  void booksCancelsAndReschedulesAppointmentThroughRegistry() {
    TestData data = createWorkflowData();
    ScheduleSlot firstSlot = createFutureSlot(data.doctor(), 1);
    ScheduleSlot secondSlot = createFutureSlot(data.doctor(), 2);

    BookAppointmentResult booking =
        appointmentBookingService.book(
            new BookAppointmentCommand(
                data.doctor().id(),
                data.patient().id(),
                firstSlot.id(),
                AppointmentSource.REGISTRY));

    assertThat(booking.available()).isTrue();
    assertThat(scheduleSlotRepository.findById(firstSlot.id()))
        .hasValueSatisfying(slot -> assertThat(slot.status()).isEqualTo(SlotStatus.BOOKED));

    BookAppointmentResult reschedule =
        appointmentManagementService.rescheduleByRegistry(
            booking.appointmentId(), data.doctor().id(), secondSlot.id());

    assertThat(reschedule.available()).isTrue();
    assertThat(appointmentRepository.findById(booking.appointmentId()))
        .hasValueSatisfying(
            appointment -> {
              assertThat(appointment.status()).isEqualTo(AppointmentStatus.CANCELLED);
              assertThat(appointment.cancelReason()).isEqualTo("Перенесено регистратурой");
            });
    assertThat(scheduleSlotRepository.findById(firstSlot.id()))
        .hasValueSatisfying(slot -> assertThat(slot.status()).isEqualTo(SlotStatus.AVAILABLE));
    assertThat(scheduleSlotRepository.findById(secondSlot.id()))
        .hasValueSatisfying(slot -> assertThat(slot.status()).isEqualTo(SlotStatus.BOOKED));

    BookAppointmentResult cancellation =
        appointmentManagementService.cancelByRegistry(
            reschedule.appointmentId(), "Пациент отказался от приема");

    assertThat(cancellation.available()).isTrue();
    assertThat(appointmentRepository.findById(reschedule.appointmentId()))
        .hasValueSatisfying(
            appointment -> {
              assertThat(appointment.status()).isEqualTo(AppointmentStatus.CANCELLED);
              assertThat(appointment.cancelReason()).isEqualTo("Пациент отказался от приема");
            });
    assertThat(scheduleSlotRepository.findById(secondSlot.id()))
        .hasValueSatisfying(slot -> assertThat(slot.status()).isEqualTo(SlotStatus.AVAILABLE));
  }

  @Test
  void completesAppointmentOnlyForAssignedDoctor() {
    TestData data = createWorkflowData();
    ScheduleSlot slot = createFutureSlot(data.doctor(), 1);
    BookAppointmentResult booking =
        appointmentBookingService.book(
            new BookAppointmentCommand(
                data.doctor().id(), data.patient().id(), slot.id(), AppointmentSource.ONLINE));
    Doctor anotherDoctor =
        doctorRepository.create(data.specialization().id(), "Другой врач", "404");

    int wrongDoctorRows =
        appointmentRepository.updateStatusForDoctor(
            booking.appointmentId(), anotherDoctor.id(), AppointmentStatus.COMPLETED);
    int assignedDoctorRows =
        appointmentRepository.updateStatusForDoctor(
            booking.appointmentId(), data.doctor().id(), AppointmentStatus.COMPLETED);

    assertThat(wrongDoctorRows).isZero();
    assertThat(assignedDoctorRows).isEqualTo(1);
    assertThat(appointmentRepository.findById(booking.appointmentId()))
        .hasValueSatisfying(
            appointment -> assertThat(appointment.status()).isEqualTo(AppointmentStatus.COMPLETED));
  }

  @Test
  void generatesScheduleDayAndReportsSkippedSlots() {
    TestData data = createWorkflowData();
    LocalDate date = LocalDate.now().plusDays(5);

    ScheduleGenerationResult firstGeneration =
        scheduleService.generateSlots(
            data.doctor().id(),
            date,
            LocalTime.of(9, 0),
            LocalTime.of(11, 0),
            30,
            LocalTime.of(10, 0),
            LocalTime.of(10, 30));
    ScheduleGenerationResult repeatedGeneration =
        scheduleService.generateSlots(
            data.doctor().id(),
            date,
            LocalTime.of(9, 0),
            LocalTime.of(11, 0),
            30,
            LocalTime.of(10, 0),
            LocalTime.of(10, 30));
    ScheduleGenerationResult pastGeneration =
        scheduleService.generateSlots(
            data.doctor().id(),
            LocalDate.now().minusDays(1),
            LocalTime.of(9, 0),
            LocalTime.of(10, 0),
            30,
            null,
            null);

    assertThat(firstGeneration).isEqualTo(new ScheduleGenerationResult(3, 0, 1, null));
    assertThat(repeatedGeneration).isEqualTo(new ScheduleGenerationResult(0, 3, 1, null));
    assertThat(pastGeneration.successful()).isFalse();
    assertThat(pastGeneration.error()).isEqualTo("Нельзя создать расписание в прошлом");
    assertThat(scheduleService.findSlotsByDoctor(data.doctor().id())).hasSize(3);
  }

  private TestData createWorkflowData() {
    Specialization specialization = specializationRepository.create("Терапия", null);
    Doctor doctor = doctorRepository.create(specialization.id(), "Иванов Иван Иванович", "101");
    Patient patient =
        patientRepository.create(
            "Петров Петр Петрович", LocalDate.of(1990, 1, 1), "79990000000", "POLICY-1");
    return new TestData(specialization, doctor, patient);
  }

  private ScheduleSlot createFutureSlot(Doctor doctor, int daysOffset) {
    LocalDateTime startTime = LocalDateTime.now().plusDays(daysOffset);
    return scheduleSlotRepository.create(
        doctor.id(), startTime, startTime.plusMinutes(30), SlotStatus.AVAILABLE);
  }

  private record TestData(Specialization specialization, Doctor doctor, Patient patient) {}
}
