package io.github.zapolyarnydev.medicalappointment.shared.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.organization")
public record OrganizationProperties(
    @DefaultValue("Система записи на прием") @NotBlank String name,
    @DefaultValue("Адрес медицинской организации") @NotBlank String publicAddress,
    @DefaultValue("Внутренний контур медицинской организации") @NotBlank String internalAddress,
    @DefaultValue @NestedConfigurationProperty Appointments appointments) {

  public record Appointments(
      @DefaultValue("30") @Min(5) int defaultDurationMinutes,
      @DefaultValue("09:00") @NotNull LocalTime defaultWorkStart,
      @DefaultValue("17:00") @NotNull LocalTime defaultWorkEnd,
      @DefaultValue("13:00") @NotNull LocalTime defaultBreakStart,
      @DefaultValue("14:00") @NotNull LocalTime defaultBreakEnd) {}
}
