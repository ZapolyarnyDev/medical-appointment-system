package io.github.zapolyarnydev.medicalappointment.ui;

import io.github.zapolyarnydev.medicalappointment.appointment.AppointmentQueryService;
import io.github.zapolyarnydev.medicalappointment.appointment.BookAppointmentResult;
import io.github.zapolyarnydev.medicalappointment.appointment.CurrentPatientAppointmentService;
import io.github.zapolyarnydev.medicalappointment.doctor.DoctorRepository;
import io.github.zapolyarnydev.medicalappointment.identity.CurrentUserService;
import io.github.zapolyarnydev.medicalappointment.identity.PatientAccount;
import io.github.zapolyarnydev.medicalappointment.identity.PatientAccountRepository;
import io.github.zapolyarnydev.medicalappointment.patient.PatientRepository;
import io.github.zapolyarnydev.medicalappointment.schedule.ScheduleSlotRepository;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
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
public class AppointmentUiController {

  private final AppointmentQueryService appointmentQueryService;
  private final CurrentPatientAppointmentService currentPatientAppointmentService;
  private final DoctorRepository doctorRepository;
  private final PatientRepository patientRepository;
  private final PatientAccountRepository patientAccountRepository;
  private final ScheduleSlotRepository scheduleSlotRepository;
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

  @GetMapping("/account/appointments/confirm")
  public String confirmCurrentPatientAppointment(
      @RequestParam(required = false) Long doctorId,
      @RequestParam(required = false) Long slotId,
      Model model,
      Principal principal,
      RedirectAttributes redirectAttributes) {
    if (currentUserService.patientAccount(principal).isEmpty()) {
      redirectAttributes.addFlashAttribute(
          "error", "Создайте профиль пациента перед записью на прием");
      return "redirect:/account";
    }
    if (doctorId == null || slotId == null) {
      redirectAttributes.addFlashAttribute("error", "Выберите врача и свободное время");
      return "redirect:/booking";
    }

    var doctor = doctorRepository.findById(doctorId);
    var slot = scheduleSlotRepository.findById(slotId);
    if (doctor.isEmpty() || slot.isEmpty() || !slot.get().doctorId().equals(doctorId)) {
      redirectAttributes.addFlashAttribute("error", "Выбранное время недоступно");
      return "redirect:/booking";
    }

    uiSupport.addCurrentUser(model, principal);
    model.addAttribute("doctor", doctor.get());
    model.addAttribute("slot", slot.get());
    return "appointment-confirm";
  }

  @GetMapping("/account/appointments/success")
  public String currentPatientAppointmentSuccess(
      @RequestParam Long appointmentId, Model model, Principal principal) {
    uiSupport.addCurrentUser(model, principal);
    model.addAttribute("appointmentId", appointmentId);
    return "appointment-success";
  }

  @PostMapping("/account/profile")
  public String createPatientProfile(
      @RequestParam String fullName,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate birthDate,
      @RequestParam String phone,
      @RequestParam(required = false) String policyNumber,
      Principal principal,
      RedirectAttributes redirectAttributes) {
    if (currentUserService.patientAccount(principal).isPresent()) {
      redirectAttributes.addFlashAttribute("error", "Профиль уже привязан к учетной записи");
      return "redirect:/account";
    }
    if (fullName.isBlank()) {
      redirectAttributes.addFlashAttribute("error", "Укажите ФИО пациента");
      return "redirect:/account";
    }
    if (birthDate == null || birthDate.isAfter(LocalDate.now())) {
      redirectAttributes.addFlashAttribute("error", "Укажите корректную дату рождения");
      return "redirect:/account";
    }
    if (phone.isBlank()) {
      redirectAttributes.addFlashAttribute("error", "Укажите телефон пациента");
      return "redirect:/account";
    }

    try {
      var patient =
          patientRepository.create(
              fullName.trim(), birthDate, phone.trim(), blankToNull(policyNumber));
      var username =
          currentUserService
              .username(principal)
              .orElseThrow(() -> new IllegalStateException("Не определен текущий пользователь"));
      patientAccountRepository.createForUsername(username, patient.id());
      redirectAttributes.addFlashAttribute("success", "Профиль пациента создан");
    } catch (DataAccessException exception) {
      redirectAttributes.addFlashAttribute("error", "Не удалось создать профиль пациента");
    }

    return "redirect:/account";
  }

  @PostMapping("/account/appointments")
  public String bookCurrentPatientAppointment(
      @RequestParam(required = false) Long doctorId,
      @RequestParam(required = false) Long slotId,
      Principal principal,
      RedirectAttributes redirectAttributes) {
    if (doctorId == null || slotId == null) {
      redirectAttributes.addFlashAttribute("error", "Выберите врача и свободное время");
      return "redirect:/booking";
    }

    BookAppointmentResult result =
        currentPatientAppointmentService.book(principal, doctorId, slotId);

    if (result.available()) {
      return "redirect:/account/appointments/success?appointmentId=" + result.appointmentId();
    }

    redirectAttributes.addFlashAttribute("error", result.message());
    return "redirect:/account/appointments/confirm?doctorId=" + doctorId + "&slotId=" + slotId;
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
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
