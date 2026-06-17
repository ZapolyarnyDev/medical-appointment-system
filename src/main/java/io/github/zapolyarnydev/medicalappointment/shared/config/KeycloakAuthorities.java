package io.github.zapolyarnydev.medicalappointment.shared.config;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

final class KeycloakAuthorities {

  private static final String ROLE_PREFIX = "ROLE_";

  private KeycloakAuthorities() {}

  static Set<GrantedAuthority> fromClaims(Map<String, Object> claims) {
    return Stream.concat(realmRoles(claims).stream(), clientRoles(claims).stream())
        .map(role -> role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role)
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());
  }

  private static Set<String> realmRoles(Map<String, Object> claims) {
    Object realmAccess = claims.get("realm_access");
    return realmAccess instanceof Map<?, ?> access ? extractRoles(access) : Set.of();
  }

  private static Set<String> clientRoles(Map<String, Object> claims) {
    Object resourceAccess = claims.get("resource_access");
    if (!(resourceAccess instanceof Map<?, ?> accessByClient)) {
      return Set.of();
    }

    return accessByClient.values().stream()
        .filter(Map.class::isInstance)
        .map(Map.class::cast)
        .flatMap(access -> extractRoles(access).stream())
        .collect(Collectors.toSet());
  }

  private static Set<String> extractRoles(Map<?, ?> access) {
    if (!(access.get("roles") instanceof Collection<?> roles)) {
      return Set.of();
    }

    return roles.stream()
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .collect(Collectors.toSet());
  }
}
