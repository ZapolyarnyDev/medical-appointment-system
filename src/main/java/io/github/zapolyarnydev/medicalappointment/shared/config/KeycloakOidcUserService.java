package io.github.zapolyarnydev.medicalappointment.shared.config;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class KeycloakOidcUserService extends OidcUserService {

  private static final String ROLE_PREFIX = "ROLE_";

  private final JsonMapper jsonMapper;

  @Override
  public OidcUser loadUser(OidcUserRequest userRequest) {
    OidcUser user = super.loadUser(userRequest);
    Set<GrantedAuthority> authorities =
        Stream.concat(user.getAuthorities().stream(), tokenRoles(userRequest).stream())
            .collect(Collectors.toSet());

    return new DefaultOidcUser(
        authorities, user.getIdToken(), user.getUserInfo(), "preferred_username");
  }

  private @NotNull Set<GrantedAuthority> tokenRoles(@NotNull OidcUserRequest userRequest) {
    try {
      String[] tokenParts = userRequest.getAccessToken().getTokenValue().split("\\.");
      if (tokenParts.length < 2) {
        return Set.of();
      }

      byte[] payload = Base64.getUrlDecoder().decode(tokenParts[1]);
      Map<?, ?> claims =
          jsonMapper.readValue(new String(payload, StandardCharsets.UTF_8), Map.class);

      return Stream.concat(realmRoles(claims).stream(), clientRoles(claims).stream())
          .map(role -> role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role)
          .map(SimpleGrantedAuthority::new)
          .collect(Collectors.toSet());
    } catch (RuntimeException exception) {
      return Set.of();
    }
  }

  private Set<String> realmRoles(Map<?, ?> claims) {
    if (!(claims.get("realm_access") instanceof Map<?, ?> realmAccess)) {
      return Set.of();
    }

    return extractRoles(realmAccess);
  }

  private Set<String> clientRoles(Map<?, ?> claims) {
    if (!(claims.get("resource_access") instanceof Map<?, ?> resourceAccess)) {
      return Set.of();
    }

    return resourceAccess.values().stream()
        .filter(Map.class::isInstance)
        .map(Map.class::cast)
        .flatMap(access -> extractRoles(access).stream())
        .collect(Collectors.toSet());
  }

  private Set<String> extractRoles(Map<?, ?> access) {
    if (!(access.get("roles") instanceof Collection<?> roles)) {
      return Set.of();
    }

    return roles.stream()
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .collect(Collectors.toSet());
  }
}
