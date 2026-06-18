package io.github.zapolyarnydev.medicalappointment.appointment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.zapolyarnydev.medicalappointment.identity.CurrentUserService;
import io.github.zapolyarnydev.medicalappointment.identity.PatientAccount;
import io.github.zapolyarnydev.medicalappointment.schedule.ScheduleSlot;
import io.github.zapolyarnydev.medicalappointment.schedule.ScheduleSlotRepository;
import io.github.zapolyarnydev.medicalappointment.schedule.SlotStatus;
import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CurrentPatientAppointmentServiceSecurityTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-01-01T09:00:00Z"), ZoneOffset.UTC);
  private static final Principal PRINCIPAL = () -> "patient";

  @Mock private AppointmentBookingService appointmentBookingService;
  @Mock private AppointmentRepository appointmentRepository;
  @Mock private ScheduleSlotRepository scheduleSlotRepository;
  @Mock private CurrentUserService currentUserService;

  @Test
  void rejectsCancellingOtherPatientAppointment() {
    CurrentPatientAppointmentService service = service();
    when(currentUserService.patientAccount(PRINCIPAL)).thenReturn(Optional.of(patientAccount(10L)));
    when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment(100L, 20L, 30L)));

    BookAppointmentResult result = service.cancel(PRINCIPAL, 100L);

    assertThat(result.available()).isFalse();
    assertThat(result.message()).isEqualTo("Запись не найдена");
    verify(appointmentRepository, never())
        .updateStatus(
            org.mockito.ArgumentMatchers.anyLong(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any());
    verify(scheduleSlotRepository, never())
        .updateStatus(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  void rejectsCancellingPastAppointment() {
    CurrentPatientAppointmentService service = service();
    when(currentUserService.patientAccount(PRINCIPAL)).thenReturn(Optional.of(patientAccount(10L)));
    when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment(100L, 10L, 30L)));
    when(scheduleSlotRepository.findById(30L))
        .thenReturn(
            Optional.of(
                new ScheduleSlot(
                    30L,
                    1L,
                    LocalDateTime.ofInstant(CLOCK.instant(), CLOCK.getZone()).minusDays(1),
                    LocalDateTime.ofInstant(CLOCK.instant(), CLOCK.getZone())
                        .minusDays(1)
                        .plusMinutes(30),
                    SlotStatus.BOOKED)));

    BookAppointmentResult result = service.cancel(PRINCIPAL, 100L);

    assertThat(result.available()).isFalse();
    assertThat(result.message()).isEqualTo("Прошедшую запись нельзя отменить");
    verify(appointmentRepository, never())
        .updateStatus(
            org.mockito.ArgumentMatchers.anyLong(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any());
  }

  private PatientAccount patientAccount(Long patientId) {
    return new PatientAccount(1L, "sub", "patient", patientId, true, LocalDateTime.now(CLOCK));
  }

  private Appointment appointment(Long id, Long patientId, Long slotId) {
    return new Appointment(
        id,
        patientId,
        slotId,
        AppointmentStatus.CREATED,
        AppointmentSource.ONLINE,
        LocalDateTime.now(CLOCK),
        null);
  }

  private CurrentPatientAppointmentService service() {
    return new CurrentPatientAppointmentService(
        appointmentBookingService,
        appointmentRepository,
        scheduleSlotRepository,
        currentUserService,
        CLOCK);
  }
}
