package io.github.zapolyarnydev.medicalappointment.ui;

import io.github.zapolyarnydev.medicalappointment.appointment.AppointmentSource;
import io.github.zapolyarnydev.medicalappointment.appointment.AppointmentStatus;
import io.github.zapolyarnydev.medicalappointment.identity.StaffRole;
import io.github.zapolyarnydev.medicalappointment.schedule.SlotStatus;
import io.github.zapolyarnydev.medicalappointment.shared.config.OrganizationProperties;
import java.security.Principal;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
@RequiredArgsConstructor
public class UiSupport {

  private final OrganizationProperties organizationProperties;

  public void addCurrentUser(@NotNull Model model, Principal principal) {
    model.addAttribute("organization", organizationProperties);
    model.addAttribute("username", principal == null ? null : principal.getName());
    model.addAttribute("authenticated", principal != null);
    model.addAttribute("roles", roles(principal));
    model.addAttribute("patient", hasAnyRole(principal, "PATIENT"));
    model.addAttribute("doctor", hasAnyRole(principal, "DOCTOR"));
    model.addAttribute("registrar", hasAnyRole(principal, "REGISTRAR", "CHIEF_DOCTOR"));
    model.addAttribute("chiefDoctor", hasAnyRole(principal, "CHIEF_DOCTOR"));
    model.addAttribute("labels", this);
  }

  public String appointmentStatus(AppointmentStatus status) {
    return switch (status) {
      case CREATED -> "Создана";
      case CANCELLED -> "Отменена";
      case COMPLETED -> "Завершена";
    };
  }

  public String appointmentSource(AppointmentSource source) {
    return switch (source) {
      case ONLINE -> "Онлайн";
      case REGISTRY -> "Регистратура";
    };
  }

  public String slotStatus(SlotStatus status) {
    return switch (status) {
      case AVAILABLE -> "Свободен";
      case BOOKED -> "Занят";
    };
  }

  public String staffRole(StaffRole role) {
    return switch (role) {
      case DOCTOR -> "Врач";
      case REGISTRAR -> "Регистратор";
      case CHIEF_DOCTOR -> "Главный врач";
    };
  }

  private boolean hasAnyRole(Principal principal, String... roles) {
    Set<String> authorities = roles(principal);
    for (String role : roles) {
      if (authorities.contains("ROLE_" + role)) {
        return true;
      }
    }
    return false;
  }

  private Set<String> roles(Principal principal) {
    if (!(principal instanceof Authentication authentication)) {
      return Set.of();
    }

    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(java.util.stream.Collectors.toSet());
  }
}
