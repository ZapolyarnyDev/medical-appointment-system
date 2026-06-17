package io.github.zapolyarnydev.medicalappointment.domain;

import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Doctor(
    @NotNull Long id,
    @NotNull Long specializationId,
    @NotNull String fullName,
    @Nullable String cabinet,
    boolean active,
    @NotNull LocalDateTime createdAt) {}
