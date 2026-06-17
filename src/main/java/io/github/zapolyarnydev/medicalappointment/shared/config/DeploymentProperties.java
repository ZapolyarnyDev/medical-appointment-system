package io.github.zapolyarnydev.medicalappointment.shared.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.deployment")
public record DeploymentProperties(
    @DefaultValue("local") @NotBlank String profile,
    @NestedConfigurationProperty Endpoints endpoints,
    @NestedConfigurationProperty Operations operations) {

  public record Endpoints(
      @DefaultValue("http://public.localhost:8080") @NotBlank String publicBaseUrl,
      @DefaultValue("http://internal.localhost:8080") @NotBlank String internalBaseUrl,
      @DefaultValue("http://auth.localhost:8080") @NotBlank String authBaseUrl) {}

  public record Operations(
      @DefaultValue("false") boolean requireSecureCookies,
      @DefaultValue("false") boolean exposeDiagnostics) {}
}
