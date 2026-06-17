package io.github.zapolyarnydev.medicalappointment.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.context.properties.bind.Name;

@ConfigurationProperties(prefix = "app.identity")
public record IdentityProperties(
    @DefaultValue("LOCAL") IdentityMode mode,
    @NestedConfigurationProperty Urls urls,
    @NestedConfigurationProperty Clients clients) {

  public record Urls(
      @DefaultValue("http://localhost:8080") String publicUrl,
      @DefaultValue("http://localhost:8080") String internalUrl) {}

  public record Clients(
      @Name("public") @DefaultValue("medical-appointment-public") String publicClientId,
      @DefaultValue("medical-appointment-internal") String internalClientId) {}
}
