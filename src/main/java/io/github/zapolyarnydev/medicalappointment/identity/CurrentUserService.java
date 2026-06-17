package io.github.zapolyarnydev.medicalappointment.identity;

import java.security.Principal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

  private final PatientAccountRepository patientAccountRepository;
  private final StaffAccountRepository staffAccountRepository;

  public @NotNull Optional<PatientAccount> patientAccount(Principal principal) {
    return subject(principal)
        .flatMap(patientAccountRepository::findActiveByKeycloakSubject)
        .or(() -> username(principal).flatMap(patientAccountRepository::findActiveByUsername));
  }

  public @NotNull Optional<StaffAccount> staffAccount(Principal principal) {
    return subject(principal)
        .flatMap(staffAccountRepository::findActiveByKeycloakSubject)
        .or(() -> username(principal).flatMap(staffAccountRepository::findActiveByUsername));
  }

  private Optional<String> username(Principal principal) {
    return principal == null ? Optional.empty() : Optional.of(principal.getName());
  }

  private Optional<String> subject(Principal principal) {
    if (principal instanceof Authentication authentication) {
      Object user = authentication.getPrincipal();
      if (user instanceof OidcUser oidcUser) {
        return Optional.of(oidcUser.getSubject());
      }
      if (user instanceof Jwt jwt) {
        return Optional.of(jwt.getSubject());
      }
    }
    return Optional.empty();
  }
}
