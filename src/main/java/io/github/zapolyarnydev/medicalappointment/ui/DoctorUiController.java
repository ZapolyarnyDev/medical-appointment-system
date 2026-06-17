package io.github.zapolyarnydev.medicalappointment.ui;

import io.github.zapolyarnydev.medicalappointment.appointment.AppointmentQueryService;
import io.github.zapolyarnydev.medicalappointment.appointment.AppointmentRepository;
import io.github.zapolyarnydev.medicalappointment.appointment.AppointmentStatus;
import io.github.zapolyarnydev.medicalappointment.doctor.DoctorRepository;
import io.github.zapolyarnydev.medicalappointment.identity.CurrentUserService;
import io.github.zapolyarnydev.medicalappointment.identity.StaffAccount;
import java.security.Principal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class DoctorUiController {

  private final CurrentUserService currentUserService;
  private final DoctorRepository doctorRepository;
  private final AppointmentQueryService appointmentQueryService;
  private final AppointmentRepository appointmentRepository;
  private final UiSupport uiSupport;

  @GetMapping("/internal/doctor")
  public String doctorWorkspace(Model model, Principal principal) {
    uiSupport.addCurrentUser(model, principal);

    var staffAccount = currentUserService.staffAccount(principal);
    var doctorId = staffAccount.map(StaffAccount::doctorId).orElse(null);
    model.addAttribute("doctorAccountMissing", doctorId == null);
    model.addAttribute(
        "doctorCard", doctorId == null ? null : doctorRepository.findById(doctorId).orElse(null));
    model.addAttribute(
        "appointments",
        doctorId == null
            ? java.util.List.of()
            : appointmentQueryService.findDetailsByDoctorIdFrom(
                doctorId, LocalDate.now().atStartOfDay()));

    return "doctor-workspace";
  }

  @PostMapping("/internal/doctor/appointments/complete")
  public String completeAppointment(
      @RequestParam Long appointmentId,
      Principal principal,
      RedirectAttributes redirectAttributes) {
    var doctorId = currentUserService.staffAccount(principal).map(StaffAccount::doctorId);
    if (doctorId.isEmpty()) {
      redirectAttributes.addFlashAttribute(
          "error", "Учетная запись врача не привязана к карточке врача");
      return "redirect:/internal/doctor";
    }

    int updatedRows =
        appointmentRepository.updateStatusForDoctor(
            appointmentId, doctorId.get(), AppointmentStatus.COMPLETED);
    redirectAttributes.addFlashAttribute(
        updatedRows == 0 ? "error" : "success",
        updatedRows == 0 ? "Запись не найдена среди приемов врача" : "Прием завершен");
    return "redirect:/internal/doctor";
  }
}
