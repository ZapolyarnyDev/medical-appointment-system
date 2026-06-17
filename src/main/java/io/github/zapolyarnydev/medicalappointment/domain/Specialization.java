package io.github.zapolyarnydev.medicalappointment.domain;

import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Specialization(
    @NotNull Long id,
    @NotNull String name,
    @Nullable String description,
    @NotNull LocalDateTime createdAt) {}
