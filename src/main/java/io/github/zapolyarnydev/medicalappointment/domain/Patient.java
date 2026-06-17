package io.github.zapolyarnydev.medicalappointment.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Patient(
    @NotNull Long id,
    @NotNull String fullName,
    @NotNull LocalDate birthDate,
    @NotNull String phone,
    @Nullable String policyNumber,
    @NotNull LocalDateTime createdAt) {}
