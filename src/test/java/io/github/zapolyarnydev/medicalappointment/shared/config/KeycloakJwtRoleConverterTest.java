package io.github.zapolyarnydev.medicalappointment.shared.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class KeycloakJwtRoleConverterTest {

  private final KeycloakJwtRoleConverter converter = new KeycloakJwtRoleConverter();

  @Test
  void convertsRealmAndClientRolesToSpringAuthorities() {
    Jwt jwt =
        new Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(60),
            Map.of("alg", "none"),
            Map.of(
                "realm_access",
                Map.of("roles", List.of("PATIENT")),
                "resource_access",
                Map.of("medical-appointment-system", Map.of("roles", List.of("REGISTRAR")))));

    Collection<GrantedAuthority> authorities = converter.convert(jwt);

    assertThat(authorities)
        .extracting(GrantedAuthority::getAuthority)
        .containsExactlyInAnyOrder("ROLE_PATIENT", "ROLE_REGISTRAR");
  }
}
