package io.github.zapolyarnydev.medicalappointment.ui;

import io.github.zapolyarnydev.medicalappointment.appointment.AppointmentQueryService;
import io.github.zapolyarnydev.medicalappointment.patient.PatientRepository;
import java.security.Principal;
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
  private final UiSupport uiSupport;

  @GetMapping("/ui/appointments")
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
