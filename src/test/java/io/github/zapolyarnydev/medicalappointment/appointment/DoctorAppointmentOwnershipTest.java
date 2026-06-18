package io.github.zapolyarnydev.medicalappointment.appointment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.zapolyarnydev.medicalappointment.identity.CurrentUserService;
import io.github.zapolyarnydev.medicalappointment.identity.StaffAccount;
import io.github.zapolyarnydev.medicalappointment.identity.StaffRole;
import io.github.zapolyarnydev.medicalappointment.ui.DoctorUiController;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ExtendWith(MockitoExtension.class)
class DoctorAppointmentOwnershipTest {

  private static final Principal PRINCIPAL = () -> "doctor";

  @Mock private CurrentUserService currentUserService;
  @Mock private io.github.zapolyarnydev.medicalappointment.doctor.DoctorRepository doctorRepository;
  @Mock private AppointmentQueryService appointmentQueryService;
  @Mock private AppointmentRepository appointmentRepository;
  @Mock private io.github.zapolyarnydev.medicalappointment.ui.UiSupport uiSupport;
  @Mock private RedirectAttributes redirectAttributes;

  @Test
  void completesOnlyAppointmentOwnedByCurrentDoctor() {
    DoctorUiController controller =
        new DoctorUiController(
            currentUserService,
            doctorRepository,
            appointmentQueryService,
            appointmentRepository,
            uiSupport);
    when(currentUserService.staffAccount(PRINCIPAL)).thenReturn(Optional.of(staffAccount(7L)));
    when(appointmentRepository.updateStatusForDoctor(100L, 7L, AppointmentStatus.COMPLETED))
        .thenReturn(0);

    String view = controller.completeAppointment(100L, PRINCIPAL, redirectAttributes);

    assertThat(view).isEqualTo("redirect:/internal/doctor");
    verify(appointmentRepository).updateStatusForDoctor(100L, 7L, AppointmentStatus.COMPLETED);
    verify(redirectAttributes).addFlashAttribute("error", "Запись не найдена среди приемов врача");
  }

  @Test
  void rejectsCompletionWhenDoctorIsNotMappedToCard() {
    DoctorUiController controller =
        new DoctorUiController(
            currentUserService,
            doctorRepository,
            appointmentQueryService,
            appointmentRepository,
            uiSupport);
    when(currentUserService.staffAccount(PRINCIPAL))
        .thenReturn(
            Optional.of(
                new StaffAccount(
                    1L, "sub", "doctor", StaffRole.DOCTOR, null, true, LocalDateTime.now())));

    String view = controller.completeAppointment(100L, PRINCIPAL, redirectAttributes);

    assertThat(view).isEqualTo("redirect:/internal/doctor");
    verify(appointmentRepository, org.mockito.Mockito.never())
        .updateStatusForDoctor(
            ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong(), ArgumentMatchers.any());
    verify(redirectAttributes)
        .addFlashAttribute("error", "Учетная запись врача не привязана к карточке врача");
  }

  private StaffAccount staffAccount(Long doctorId) {
    return new StaffAccount(
        1L, "sub", "doctor", StaffRole.DOCTOR, doctorId, true, LocalDateTime.now());
  }
}
