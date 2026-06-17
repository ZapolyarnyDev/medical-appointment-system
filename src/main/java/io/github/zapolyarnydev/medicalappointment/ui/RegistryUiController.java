package io.github.zapolyarnydev.medicalappointment.ui;

import io.github.zapolyarnydev.medicalappointment.appointment.AppointmentBookingService;
import io.github.zapolyarnydev.medicalappointment.appointment.AppointmentQueryService;
import io.github.zapolyarnydev.medicalappointment.appointment.AppointmentSource;
import io.github.zapolyarnydev.medicalappointment.appointment.BookAppointmentCommand;
import io.github.zapolyarnydev.medicalappointment.appointment.BookAppointmentResult;
import io.github.zapolyarnydev.medicalappointment.doctor.DoctorRepository;
import io.github.zapolyarnydev.medicalappointment.patient.PatientRepository;
import io.github.zapolyarnydev.medicalappointment.schedule.ScheduleService;
import java.security.Principal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class RegistryUiController {

  private final PatientRepository patientRepository;
  private final DoctorRepository doctorRepository;
  private final ScheduleService scheduleService;
  private final AppointmentBookingService appointmentBookingService;
  private final AppointmentQueryService appointmentQueryService;
  private final UiSupport uiSupport;

  @GetMapping("/ui/registry")
  public String registry(
      @RequestParam(required = false) Long doctorId, Model model, Principal principal) {
    uiSupport.addCurrentUser(model, principal);
    model.addAttribute("patients", patientRepository.findAll());
    model.addAttribute("doctors", doctorRepository.findAll());
    model.addAttribute("appointments", appointmentQueryService.findDetails());
    model.addAttribute("selectedDoctorId", doctorId);

    if (doctorId != null) {
      model.addAttribute("slots", scheduleService.findAvailableFutureSlotsByDoctor(doctorId));
    }

    return "registry";
  }

  @PostMapping("/ui/registry/patients")
  public String createPatient(
      @RequestParam String fullName,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
      @RequestParam String phone,
      @RequestParam(required = false) String policyNumber,
      RedirectAttributes redirectAttributes) {
    patientRepository.create(fullName, birthDate, phone, blankToNull(policyNumber));
    redirectAttributes.addFlashAttribute("success", "Пациент добавлен");
    return "redirect:/ui/registry";
  }

  @PostMapping("/ui/registry/appointments")
  public String bookAppointment(
      @RequestParam Long doctorId,
      @RequestParam Long patientId,
      @RequestParam Long slotId,
      RedirectAttributes redirectAttributes) {
    BookAppointmentResult result =
        appointmentBookingService.book(
            new BookAppointmentCommand(doctorId, patientId, slotId, AppointmentSource.REGISTRY));
    redirectAttributes.addFlashAttribute(
        result.available() ? "success" : "error", result.message());
    return "redirect:/ui/registry?doctorId=" + doctorId;
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
