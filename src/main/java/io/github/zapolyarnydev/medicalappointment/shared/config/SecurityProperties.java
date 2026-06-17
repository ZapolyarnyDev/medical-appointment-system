package io.github.zapolyarnydev.medicalappointment.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(boolean publicDocsEnabled) {}
