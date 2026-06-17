package io.github.zapolyarnydev.medicalappointment.ui;

import io.github.zapolyarnydev.medicalappointment.appointment.AppointmentRepository;
import io.github.zapolyarnydev.medicalappointment.appointment.AppointmentStatus;
import io.github.zapolyarnydev.medicalappointment.doctor.DoctorRepository;
import io.github.zapolyarnydev.medicalappointment.schedule.ScheduleService;
import io.github.zapolyarnydev.medicalappointment.specialization.SpecializationService;
import java.security.Principal;
import java.time.LocalDateTime;
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
public class AdminUiController {

  private final SpecializationService specializationService;
  private final DoctorRepository doctorRepository;
  private final ScheduleService scheduleService;
  private final AppointmentRepository appointmentRepository;
  private final UiSupport uiSupport;

  @GetMapping("/admin")
  public String admin(
      @RequestParam(required = false) Long doctorId, Model model, Principal principal) {
    uiSupport.addCurrentUser(model, principal);
    model.addAttribute("specializations", specializationService.findSpecializations());
    model.addAttribute("doctors", doctorRepository.findAll());
    model.addAttribute("selectedDoctorId", doctorId);
    model.addAttribute("appointments", appointmentRepository.findDetails());

    if (doctorId != null) {
      model.addAttribute("slots", scheduleService.findSlotsByDoctor(doctorId));
    }

    return "admin";
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
    scheduleService.createSlot(doctorId, startTime, durationMinutes);
    redirectAttributes.addFlashAttribute("success", "Слот расписания добавлен");
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
