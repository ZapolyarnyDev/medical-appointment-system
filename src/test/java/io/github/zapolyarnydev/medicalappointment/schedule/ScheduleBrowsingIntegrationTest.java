package io.github.zapolyarnydev.medicalappointment.schedule;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.zapolyarnydev.medicalappointment.doctor.Doctor;
import io.github.zapolyarnydev.medicalappointment.doctor.DoctorRepository;
import io.github.zapolyarnydev.medicalappointment.shared.PostgresIntegrationTest;
import io.github.zapolyarnydev.medicalappointment.specialization.Specialization;
import io.github.zapolyarnydev.medicalappointment.specialization.SpecializationRepository;
import io.github.zapolyarnydev.medicalappointment.specialization.SpecializationService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ScheduleBrowsingIntegrationTest extends PostgresIntegrationTest {

  @Autowired private SpecializationRepository specializationRepository;
  @Autowired private SpecializationService specializationService;
  @Autowired private DoctorRepository doctorRepository;
  @Autowired private ScheduleSlotRepository scheduleSlotRepository;
  @Autowired private ScheduleService scheduleService;

  @Test
  void returnsSpecializationsAndActiveDoctorsBySpecialization() {
    Specialization therapy = specializationRepository.create("Терапия", "Общий прием");
    Specialization surgery = specializationRepository.create("Хирургия", null);
    Doctor doctor = doctorRepository.create(therapy.id(), "Иванов Иван Иванович", "101");
    doctorRepository.create(surgery.id(), "Петров Петр Петрович", "202");

    assertThat(specializationService.findSpecializations())
        .extracting(Specialization::name)
        .containsExactly("Терапия", "Хирургия");
    assertThat(specializationService.findActiveDoctorsBySpecialization(therapy.id()))
        .containsExactly(doctor);
  }

  @Test
  void returnsOnlyAvailableFutureSlotsForDoctor() {
    Specialization specialization = specializationRepository.create("Кардиология", null);
    Doctor doctor = doctorRepository.create(specialization.id(), "Сидоров Сидор Сидорович", "303");
    LocalDateTime now = LocalDateTime.now();
    ScheduleSlot expected =
        scheduleSlotRepository.create(
            doctor.id(), now.plusDays(1), now.plusDays(1).plusMinutes(30), SlotStatus.AVAILABLE);
    scheduleSlotRepository.create(
        doctor.id(), now.plusDays(2), now.plusDays(2).plusMinutes(30), SlotStatus.BOOKED);
    scheduleSlotRepository.create(
        doctor.id(), now.minusDays(1), now.minusDays(1).plusMinutes(30), SlotStatus.AVAILABLE);

    List<ScheduleSlot> slots = scheduleService.findAvailableFutureSlotsByDoctor(doctor.id());

    assertThat(slots).containsExactly(expected);
  }
}
