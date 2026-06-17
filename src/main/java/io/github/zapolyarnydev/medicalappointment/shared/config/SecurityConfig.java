package io.github.zapolyarnydev.medicalappointment.shared.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

  private static final String[] OPENAPI_ENDPOINTS = {
    "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
  };

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

              authorization.anyRequest().authenticated();
            })
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        .build();
  }
}
