package io.github.zapolyarnydev.medicalappointment.ui;

import io.github.zapolyarnydev.medicalappointment.doctor.DoctorRepository;
import io.github.zapolyarnydev.medicalappointment.schedule.ScheduleService;
import io.github.zapolyarnydev.medicalappointment.specialization.SpecializationService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CatalogUiController {

  private final SpecializationService specializationService;
  private final DoctorRepository doctorRepository;
  private final ScheduleService scheduleService;
  private final UiSupport uiSupport;

  @GetMapping("/")
  public String home(Model model, Principal principal) {
    uiSupport.addCurrentUser(model, principal);
    model.addAttribute("specializations", specializationService.findSpecializations());
    return "home";
  }

  @GetMapping("/booking")
  public String catalog(
      @RequestParam(required = false) Long specializationId,
      @RequestParam(required = false) Long doctorId,
      Model model,
      Principal principal) {
    uiSupport.addCurrentUser(model, principal);
    model.addAttribute("specializations", specializationService.findSpecializations());
    model.addAttribute("selectedSpecializationId", specializationId);
    model.addAttribute("selectedDoctorId", doctorId);
    if (doctorId != null) {
      doctorRepository
          .findById(doctorId)
          .ifPresent(doctor -> model.addAttribute("selectedDoctor", doctor));
    }

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
}
