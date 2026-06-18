package io.github.zapolyarnydev.medicalappointment.shared.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jooq.autoconfigure.JooqAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(classes = RoleBasedAccessSecurityTest.TestApplication.class)
@TestPropertySource(
    properties = {
      "app.security.public-docs-enabled=false",
      "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://issuer.example/jwks",
      "spring.security.oauth2.client.provider.keycloak.authorization-uri=http://issuer.example/auth",
      "spring.security.oauth2.client.provider.keycloak.token-uri=http://issuer.example/token",
      "spring.security.oauth2.client.provider.keycloak.jwk-set-uri=http://issuer.example/jwks",
      "spring.security.oauth2.client.provider.keycloak.user-info-uri=http://issuer.example/userinfo",
      "spring.security.oauth2.client.provider.keycloak.user-name-attribute=sub",
      "spring.security.oauth2.client.registration.public.client-id=public",
      "spring.security.oauth2.client.registration.public.client-authentication-method=none",
      "spring.security.oauth2.client.registration.public.authorization-grant-type=authorization_code",
      "spring.security.oauth2.client.registration.public.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}",
      "spring.security.oauth2.client.registration.internal.client-id=internal",
      "spring.security.oauth2.client.registration.internal.client-authentication-method=none",
      "spring.security.oauth2.client.registration.internal.authorization-grant-type=authorization_code",
      "spring.security.oauth2.client.registration.internal.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}"
    })
class RoleBasedAccessSecurityTest {

  private final MockMvc mockMvc;

  RoleBasedAccessSecurityTest(WebApplicationContext context) {
    this.mockMvc =
        MockMvcBuilders.webAppContextSetup(context)
            .apply(
                org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
                    .springSecurity())
            .build();
  }

  @Test
  void redirectsAnonymousPatientAreaToPublicLogin() throws Exception {
    mockMvc
        .perform(get("/account"))
        .andExpect(status().is3xxRedirection())
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl(
                "/oauth2/authorization/public"));
  }

  @Test
  void redirectsAnonymousInternalAreaToInternalLogin() throws Exception {
    mockMvc
        .perform(get("/internal/registry"))
        .andExpect(status().is3xxRedirection())
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl(
                "/oauth2/authorization/internal"));

    mockMvc
        .perform(get("/admin"))
        .andExpect(status().is3xxRedirection())
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl(
                "/oauth2/authorization/internal"));
  }

  @Test
  void protectsInternalPagesByRole() throws Exception {
    mockMvc
        .perform(get("/internal/registry").with(oidcRole("PATIENT")))
        .andExpect(status().isForbidden());
    mockMvc
        .perform(get("/internal/registry").with(oidcRole("REGISTRAR")))
        .andExpect(status().isOk());
    mockMvc
        .perform(get("/internal/appointments").with(oidcRole("CHIEF_DOCTOR")))
        .andExpect(status().isOk());
    mockMvc.perform(get("/internal/doctor").with(oidcRole("DOCTOR"))).andExpect(status().isOk());
    mockMvc
        .perform(get("/internal/doctor").with(oidcRole("REGISTRAR")))
        .andExpect(status().isForbidden());
  }

  @Test
  void protectsAdminPagesByRole() throws Exception {
    mockMvc.perform(get("/admin").with(oidcRole("REGISTRAR"))).andExpect(status().isForbidden());
    mockMvc.perform(get("/admin").with(oidcRole("DOCTOR"))).andExpect(status().isForbidden());
    mockMvc.perform(get("/admin").with(oidcRole("CHIEF_DOCTOR"))).andExpect(status().isOk());
  }

  @Test
  void protectsPatientPagesByRole() throws Exception {
    mockMvc.perform(get("/account").with(oidcRole("PATIENT"))).andExpect(status().isOk());
    mockMvc.perform(get("/account").with(oidcRole("REGISTRAR"))).andExpect(status().isForbidden());
  }

  @Test
  void protectsAppointmentApiByRole() throws Exception {
    mockMvc
        .perform(post("/api/appointments/my/1/cancel").with(jwtRole("PATIENT")))
        .andExpect(status().isOk());
    mockMvc
        .perform(post("/api/appointments/my/1/cancel").with(jwtRole("REGISTRAR")))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(post("/api/appointments/book").with(jwtRole("PATIENT")))
        .andExpect(status().isForbidden());
    mockMvc
        .perform(post("/api/appointments/book").with(jwtRole("REGISTRAR")))
        .andExpect(status().isOk());
    mockMvc
        .perform(post("/api/appointments/book").with(jwtRole("CHIEF_DOCTOR")))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/api/appointments/patients/1").with(jwtRole("PATIENT")))
        .andExpect(status().isForbidden());
    mockMvc
        .perform(get("/api/appointments/patients/1").with(jwtRole("REGISTRAR")))
        .andExpect(status().isOk());
  }

  @Test
  void keepsOpenApiClosedWhenPublicDocsAreDisabled() throws Exception {
    mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isUnauthorized());
  }

  private static org.springframework.test.web.servlet.request.RequestPostProcessor oidcRole(
      String role) {
    return oidcLogin().authorities(new SimpleGrantedAuthority("ROLE_" + role));
  }

  private static org.springframework.test.web.servlet.request.RequestPostProcessor jwtRole(
      String role) {
    return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
  }

  @EnableAutoConfiguration(
      exclude = {
        OAuth2ClientAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        FlywayAutoConfiguration.class,
        JooqAutoConfiguration.class
      })
  @Import(SecurityConfig.class)
  static class TestApplication {

    @Bean
    GrantedAuthoritiesMapper keycloakAuthoritiesMapper() {
      return authorities -> authorities;
    }

    @Bean
    ClientRegistrationRepository clientRegistrationRepository() {
      return new InMemoryClientRegistrationRepository(
          clientRegistration("public"), clientRegistration("internal"));
    }

    @Bean
    OAuth2AuthorizedClientService authorizedClientService(
        ClientRegistrationRepository clientRegistrationRepository) {
      return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Bean
    AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager(
        ClientRegistrationRepository clientRegistrationRepository,
        OAuth2AuthorizedClientService authorizedClientService) {
      return new AuthorizedClientServiceOAuth2AuthorizedClientManager(
          clientRegistrationRepository, authorizedClientService);
    }

    @Bean
    JwtDecoder jwtDecoder() {
      return token -> {
        throw new UnsupportedOperationException("JWT decoding is not used by MockMvc jwt()");
      };
    }

    @Bean
    ProtectedRoutesController protectedRoutesController() {
      return new ProtectedRoutesController();
    }

    private ClientRegistration clientRegistration(String registrationId) {
      return ClientRegistration.withRegistrationId(registrationId)
          .clientId(registrationId)
          .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
          .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
          .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
          .authorizationUri("http://issuer.example/auth")
          .tokenUri("http://issuer.example/token")
          .jwkSetUri("http://issuer.example/jwks")
          .userInfoUri("http://issuer.example/userinfo")
          .userNameAttributeName("sub")
          .scope("openid", "profile")
          .build();
    }

    @RestController
    static class ProtectedRoutesController {

      @GetMapping({
        "/account",
        "/internal/registry",
        "/internal/appointments",
        "/internal/doctor",
        "/admin"
      })
      String page() {
        return "ok";
      }

      @PostMapping("/api/appointments/my/{appointmentId}/cancel")
      String cancelMine(@PathVariable Long appointmentId) {
        return "ok";
      }

      @PostMapping("/api/appointments/book")
      String book() {
        return "ok";
      }

      @GetMapping("/api/appointments/patients/{patientId}")
      String patientAppointments(@PathVariable Long patientId) {
        return "ok";
      }
    }
  }
}
