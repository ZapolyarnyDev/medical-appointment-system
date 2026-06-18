package io.github.zapolyarnydev.medicalappointment.ui;

import io.github.zapolyarnydev.medicalappointment.appointment.AppointmentBookingService;
import io.github.zapolyarnydev.medicalappointment.appointment.AppointmentManagementService;
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
import org.springframework.dao.DataAccessException;
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
  private final AppointmentManagementService appointmentManagementService;
  private final AppointmentQueryService appointmentQueryService;
  private final UiSupport uiSupport;

  @GetMapping("/internal/registry")
  public String registry(
      @RequestParam(required = false) Long doctorId,
      @RequestParam(required = false) Long patientId,
      @RequestParam(required = false) String patientQuery,
      Model model,
      Principal principal) {
    uiSupport.addCurrentUser(model, principal);
    model.addAttribute(
        "patients",
        patientQuery == null || patientQuery.isBlank()
            ? patientRepository.findAll()
            : patientRepository.search(patientQuery));
    model.addAttribute("doctors", doctorRepository.findAll());
    model.addAttribute(
        "appointments",
        patientId == null
            ? appointmentQueryService.findDetails()
            : appointmentQueryService.findDetailsByPatientId(patientId));
    model.addAttribute("selectedDoctorId", doctorId);
    model.addAttribute("selectedPatientId", patientId);
    model.addAttribute("patientQuery", patientQuery);

    if (doctorId != null) {
      model.addAttribute("slots", scheduleService.findAvailableFutureSlotsByDoctor(doctorId));
    }

    return "registry";
  }

  @PostMapping("/internal/registry/patients")
  public String createPatient(
      @RequestParam String fullName,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
      @RequestParam String phone,
      @RequestParam(required = false) String policyNumber,
      RedirectAttributes redirectAttributes) {
    try {
      patientRepository.create(fullName.trim(), birthDate, phone.trim(), blankToNull(policyNumber));
      redirectAttributes.addFlashAttribute("success", "Пациент добавлен");
    } catch (DataAccessException exception) {
      redirectAttributes.addFlashAttribute(
          "error", "Пациент с таким телефоном или полисом уже существует");
    }
    return "redirect:/internal/registry";
  }

  @PostMapping("/internal/registry/appointments")
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
    return "redirect:/internal/registry?doctorId=" + doctorId;
  }

  @PostMapping("/internal/registry/appointments/cancel")
  public String cancelAppointment(
      @RequestParam Long appointmentId,
      @RequestParam(required = false) String cancelReason,
      @RequestParam(required = false) Long doctorId,
      RedirectAttributes redirectAttributes) {
    BookAppointmentResult result =
        appointmentManagementService.cancelByRegistry(appointmentId, cancelReason);
    redirectAttributes.addFlashAttribute(
        result.available() ? "success" : "error", result.message());
    return redirectToRegistry(doctorId);
  }

  @PostMapping("/internal/registry/appointments/reschedule")
  public String rescheduleAppointment(
      @RequestParam Long appointmentId,
      @RequestParam Long doctorId,
      @RequestParam Long slotId,
      RedirectAttributes redirectAttributes) {
    BookAppointmentResult result =
        appointmentManagementService.rescheduleByRegistry(appointmentId, doctorId, slotId);
    redirectAttributes.addFlashAttribute(
        result.available() ? "success" : "error",
        result.available() ? "Запись перенесена" : result.message());
    return redirectToRegistry(doctorId);
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  private String redirectToRegistry(Long doctorId) {
    return doctorId == null
        ? "redirect:/internal/registry"
        : "redirect:/internal/registry?doctorId=" + doctorId;
  }
}
