package io.github.zapolyarnydev.medicalappointment.shared.config;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class KeycloakJwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

  @Override
  public Collection<GrantedAuthority> convert(@NotNull Jwt jwt) {
    return KeycloakAuthorities.fromClaims(jwt.getClaims());
  }
}
