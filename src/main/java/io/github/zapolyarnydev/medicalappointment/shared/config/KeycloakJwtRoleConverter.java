package io.github.zapolyarnydev.medicalappointment.shared.config;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class KeycloakJwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

  private static final String ROLE_PREFIX = "ROLE_";

  @Override
  public Collection<GrantedAuthority> convert(@NotNull Jwt jwt) {
    return Stream.concat(realmRoles(jwt).stream(), clientRoles(jwt).stream())
        .map(role -> role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role)
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());
  }

  private Set<String> realmRoles(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
    return extractRoles(realmAccess);
  }

  private Set<String> clientRoles(Jwt jwt) {
    Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
    if (resourceAccess == null) {
      return Set.of();
    }

    return resourceAccess.values().stream()
        .filter(Map.class::isInstance)
        .map(Map.class::cast)
        .flatMap(access -> extractRoles(access).stream())
        .collect(Collectors.toSet());
  }

  private Set<String> extractRoles(Map<?, ?> access) {
    if (access == null || !(access.get("roles") instanceof Collection<?> roles)) {
      return Set.of();
    }

    return roles.stream()
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .collect(Collectors.toSet());
  }
}
