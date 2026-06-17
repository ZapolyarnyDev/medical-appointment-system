package io.github.zapolyarnydev.medicalappointment.identity;

import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PatientAccount(
    @NotNull Long id,
    @Nullable String keycloakSubject,
    @NotNull String username,
    @NotNull Long patientId,
    boolean active,
    @NotNull LocalDateTime createdAt) {}
