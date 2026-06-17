package io.github.zapolyarnydev.medicalappointment.ui;

import io.github.zapolyarnydev.medicalappointment.appointment.AppointmentQueryService;
import io.github.zapolyarnydev.medicalappointment.identity.CurrentUserService;
import io.github.zapolyarnydev.medicalappointment.identity.PatientAccount;
import io.github.zapolyarnydev.medicalappointment.patient.PatientRepository;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AppointmentUiController {

  private final AppointmentQueryService appointmentQueryService;
  private final PatientRepository patientRepository;
  private final CurrentUserService currentUserService;
  private final UiSupport uiSupport;

  @GetMapping("/account")
  public String account(Model model, Principal principal) {
    uiSupport.addCurrentUser(model, principal);
    var patientAccount = currentUserService.patientAccount(principal);
    model.addAttribute("patientAccountMissing", patientAccount.isEmpty());
    model.addAttribute(
        "appointments",
        patientAccount
            .map(PatientAccount::patientId)
            .map(appointmentQueryService::findDetailsByPatientId)
            .orElseGet(List::of));
    return "account";
  }

  @GetMapping("/internal/appointments")
  public String appointments(
      @RequestParam(required = false) Long patientId, Model model, Principal principal) {
    uiSupport.addCurrentUser(model, principal);
    model.addAttribute("patients", patientRepository.findAll());
    model.addAttribute("selectedPatientId", patientId);
    model.addAttribute(
        "appointments",
        patientId == null
            ? appointmentQueryService.findDetails()
            : appointmentQueryService.findDetailsByPatientId(patientId));
    return "appointments";
  }
}
