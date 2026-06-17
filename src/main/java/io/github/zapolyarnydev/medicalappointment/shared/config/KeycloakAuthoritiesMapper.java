package io.github.zapolyarnydev.medicalappointment.shared.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KeycloakAuthoritiesMapper implements GrantedAuthoritiesMapper {

  @Override
  public Collection<? extends GrantedAuthority> mapAuthorities(
      @NotNull Collection<? extends GrantedAuthority> authorities) {
    Set<GrantedAuthority> mappedAuthorities = new HashSet<>(authorities);

    authorities.stream()
        .filter(OidcUserAuthority.class::isInstance)
        .map(OidcUserAuthority.class::cast)
        .map(OidcUserAuthority::getIdToken)
        .map(idToken -> KeycloakAuthorities.fromClaims(idToken.getClaims()))
        .forEach(mappedAuthorities::addAll);

    return mappedAuthorities;
  }
}
