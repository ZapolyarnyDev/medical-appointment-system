package io.github.zapolyarnydev.medicalappointment.shared.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

  private static final String[] OPENAPI_ENDPOINTS = {
    "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
  };

  private static final String PATIENT = "PATIENT";
  private static final String DOCTOR = "DOCTOR";
  private static final String REGISTRAR = "REGISTRAR";
  private static final String CHIEF_DOCTOR = "CHIEF_DOCTOR";

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, SecurityProperties securityProperties) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authorization -> {
              authorization.requestMatchers("/api/health").permitAll();

              if (securityProperties.publicDocsEnabled()) {
                authorization.requestMatchers(OPENAPI_ENDPOINTS).permitAll();
              }

              authorization
                  .requestMatchers("/api/specializations/**", "/api/doctors/*/slots/available")
                  .hasAnyRole(PATIENT, REGISTRAR, CHIEF_DOCTOR)
                  .requestMatchers("/api/appointments/book")
                  .hasAnyRole(PATIENT, REGISTRAR)
                  .requestMatchers("/api/appointments/patients/**")
                  .hasAnyRole(PATIENT, REGISTRAR, CHIEF_DOCTOR)
                  .anyRequest()
                  .authenticated();
            })
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
        .build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(new KeycloakJwtRoleConverter());
    return converter;
  }
}
