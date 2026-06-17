package io.github.zapolyarnydev.medicalappointment.identity;

import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record StaffAccount(
    @NotNull Long id,
    @Nullable String keycloakSubject,
    @NotNull String username,
    @NotNull StaffRole role,
    @Nullable Long doctorId,
    boolean active,
    @NotNull LocalDateTime createdAt) {}
