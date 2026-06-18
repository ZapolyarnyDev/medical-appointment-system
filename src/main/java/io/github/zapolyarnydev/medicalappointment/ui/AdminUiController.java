package io.github.zapolyarnydev.medicalappointment.ui;

import io.github.zapolyarnydev.medicalappointment.appointment.AppointmentRepository;
import io.github.zapolyarnydev.medicalappointment.appointment.AppointmentStatus;
import io.github.zapolyarnydev.medicalappointment.doctor.DoctorRepository;
import io.github.zapolyarnydev.medicalappointment.identity.PatientAccountRepository;
import io.github.zapolyarnydev.medicalappointment.identity.StaffAccountRepository;
import io.github.zapolyarnydev.medicalappointment.identity.StaffRole;
import io.github.zapolyarnydev.medicalappointment.patient.PatientRepository;
import io.github.zapolyarnydev.medicalappointment.schedule.ScheduleService;
import io.github.zapolyarnydev.medicalappointment.shared.config.DeploymentProperties;
import io.github.zapolyarnydev.medicalappointment.shared.config.IdentityProperties;
import io.github.zapolyarnydev.medicalappointment.shared.config.OrganizationProperties;
import io.github.zapolyarnydev.medicalappointment.shared.config.SecurityProperties;
import io.github.zapolyarnydev.medicalappointment.specialization.SpecializationService;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
public class AdminUiController {

  private final SpecializationService specializationService;
  private final DoctorRepository doctorRepository;
  private final PatientRepository patientRepository;
  private final PatientAccountRepository patientAccountRepository;
  private final StaffAccountRepository staffAccountRepository;
  private final ScheduleService scheduleService;
  private final AppointmentRepository appointmentRepository;
  private final DeploymentProperties deploymentProperties;
  private final IdentityProperties identityProperties;
  private final OrganizationProperties organizationProperties;
  private final SecurityProperties securityProperties;
  private final UiSupport uiSupport;

  @GetMapping("/admin")
  public String admin(
      @RequestParam(required = false) Long doctorId, Model model, Principal principal) {
    uiSupport.addCurrentUser(model, principal);
    model.addAttribute("specializations", specializationService.findSpecializations());
    model.addAttribute("doctors", doctorRepository.findAll());
    model.addAttribute("patients", patientRepository.findAll());
    model.addAttribute("patientAccounts", patientAccountRepository.findAll());
    model.addAttribute("staffAccounts", staffAccountRepository.findAll());
    model.addAttribute("staffRoles", StaffRole.values());
    model.addAttribute("selectedDoctorId", doctorId);
    model.addAttribute("appointments", appointmentRepository.findDetails());
    model.addAttribute("deployment", deploymentProperties);
    model.addAttribute("identity", identityProperties);
    model.addAttribute("organizationSettings", organizationProperties);
    model.addAttribute("security", securityProperties);

    if (doctorId != null) {
      model.addAttribute("slots", scheduleService.findSlotsByDoctor(doctorId));
    }

    return "admin";
  }

  @PostMapping("/admin/identity/patients")
  public String createPatientAccount(
      @RequestParam String username,
      @RequestParam(required = false) String keycloakSubject,
      @RequestParam Long patientId,
      RedirectAttributes redirectAttributes) {
    try {
      patientAccountRepository.create(username.trim(), blankToNull(keycloakSubject), patientId);
      redirectAttributes.addFlashAttribute("success", "Пациентская учетная запись привязана");
    } catch (DataAccessException exception) {
      redirectAttributes.addFlashAttribute("error", "Не удалось создать привязку пациента");
    }
    return "redirect:/admin";
  }

  @PostMapping("/admin/identity/staff")
  public String createStaffAccount(
      @RequestParam String username,
      @RequestParam(required = false) String keycloakSubject,
      @RequestParam StaffRole role,
      @RequestParam(required = false) Long doctorId,
      RedirectAttributes redirectAttributes) {
    if (role == StaffRole.DOCTOR && doctorId == null) {
      redirectAttributes.addFlashAttribute("error", "Для врача нужно выбрать карточку врача");
      return "redirect:/admin";
    }

    try {
      staffAccountRepository.create(username.trim(), blankToNull(keycloakSubject), role, doctorId);
      redirectAttributes.addFlashAttribute("success", "Учетная запись сотрудника привязана");
    } catch (DataAccessException exception) {
      redirectAttributes.addFlashAttribute("error", "Не удалось создать привязку сотрудника");
    }
    return "redirect:/admin";
  }

  @PostMapping("/admin/specializations")
  public String createSpecialization(
      @RequestParam String name,
      @RequestParam(required = false) String description,
      RedirectAttributes redirectAttributes) {
    specializationService.createSpecialization(name, blankToNull(description));
    redirectAttributes.addFlashAttribute("success", "Специализация добавлена");
    return "redirect:/admin";
  }

  @PostMapping("/admin/doctors")
  public String createDoctor(
      @RequestParam Long specializationId,
      @RequestParam String fullName,
      @RequestParam(required = false) String cabinet,
      RedirectAttributes redirectAttributes) {
    doctorRepository.create(specializationId, fullName, blankToNull(cabinet));
    redirectAttributes.addFlashAttribute("success", "Врач добавлен");
    return "redirect:/admin";
  }

  @PostMapping("/admin/slots")
  public String createSlot(
      @RequestParam Long doctorId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
      @RequestParam(defaultValue = "30") int durationMinutes,
      RedirectAttributes redirectAttributes) {
    try {
      scheduleService.createSlot(doctorId, startTime, durationMinutes);
      redirectAttributes.addFlashAttribute("success", "Слот расписания добавлен");
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("error", exception.getMessage());
    }
    return "redirect:/admin?doctorId=" + doctorId;
  }

  @PostMapping("/admin/slots/batch")
  public String generateSlots(
      @RequestParam Long doctorId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime workStart,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime workEnd,
      @RequestParam(defaultValue = "30") int durationMinutes,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
          LocalTime breakStart,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
          LocalTime breakEnd,
      RedirectAttributes redirectAttributes) {
    var result =
        scheduleService.generateSlots(
            doctorId, date, workStart, workEnd, durationMinutes, breakStart, breakEnd);
    redirectAttributes.addFlashAttribute(
        result.successful() && result.createdSlots() > 0 ? "success" : "error",
        result.successful()
            ? "Создано слотов: "
                + result.createdSlots()
                + ", пропущено дублей: "
                + result.skippedDuplicateSlots()
                + ", пропущено на перерыв: "
                + result.skippedBreakSlots()
            : result.error());
    return "redirect:/admin?doctorId=" + doctorId;
  }

  @PostMapping("/admin/appointments/status")
  public String updateAppointmentStatus(
      @RequestParam Long appointmentId,
      @RequestParam AppointmentStatus status,
      @RequestParam(required = false) String cancelReason,
      RedirectAttributes redirectAttributes) {
    appointmentRepository.updateStatus(appointmentId, status, blankToNull(cancelReason));
    redirectAttributes.addFlashAttribute("success", "Статус записи обновлен");
    return "redirect:/admin";
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
