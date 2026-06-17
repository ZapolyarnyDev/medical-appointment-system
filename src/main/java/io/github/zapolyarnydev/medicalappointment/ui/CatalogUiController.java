package io.github.zapolyarnydev.medicalappointment.ui;

import io.github.zapolyarnydev.medicalappointment.appointment.AppointmentBookingService;
import io.github.zapolyarnydev.medicalappointment.appointment.AppointmentSource;
import io.github.zapolyarnydev.medicalappointment.appointment.BookAppointmentCommand;
import io.github.zapolyarnydev.medicalappointment.appointment.BookAppointmentResult;
import io.github.zapolyarnydev.medicalappointment.doctor.DoctorRepository;
import io.github.zapolyarnydev.medicalappointment.patient.PatientRepository;
import io.github.zapolyarnydev.medicalappointment.schedule.ScheduleService;
import io.github.zapolyarnydev.medicalappointment.specialization.SpecializationService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CatalogUiController {

  private final SpecializationService specializationService;
  private final DoctorRepository doctorRepository;
  private final PatientRepository patientRepository;
  private final ScheduleService scheduleService;
  private final AppointmentBookingService appointmentBookingService;
  private final UiSupport uiSupport;

  @GetMapping({"/", "/ui", "/ui/catalog"})
  public String catalog(
      @RequestParam(required = false) Long specializationId,
      @RequestParam(required = false) Long doctorId,
      Model model,
      Principal principal) {
    uiSupport.addCurrentUser(model, principal);
    model.addAttribute("specializations", specializationService.findSpecializations());
    model.addAttribute("selectedSpecializationId", specializationId);
    model.addAttribute("selectedDoctorId", doctorId);
    model.addAttribute("patients", patientRepository.findAll());

    if (specializationId != null) {
      model.addAttribute(
          "doctors", specializationService.findActiveDoctorsBySpecialization(specializationId));
    } else {
      model.addAttribute("doctors", doctorRepository.findAll());
    }

    if (doctorId != null) {
      model.addAttribute("slots", scheduleService.findAvailableFutureSlotsByDoctor(doctorId));
    }

    return "catalog";
  }

  @PostMapping("/ui/appointments/book")
  public String book(
      @RequestParam Long doctorId,
      @RequestParam Long patientId,
      @RequestParam Long slotId,
      @RequestParam(required = false) @Nullable AppointmentSource source,
      RedirectAttributes redirectAttributes) {
    BookAppointmentResult result =
        appointmentBookingService.book(
            new BookAppointmentCommand(
                doctorId, patientId, slotId, source == null ? AppointmentSource.ONLINE : source));

    redirectAttributes.addFlashAttribute(
        result.available() ? "success" : "error", result.message());
    return "redirect:/ui/catalog?doctorId=" + doctorId;
  }
}
