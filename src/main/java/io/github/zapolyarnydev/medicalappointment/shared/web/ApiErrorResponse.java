package io.github.zapolyarnydev.medicalappointment.shared.web;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public record ApiErrorResponse(@NotNull String message, @NotNull List<String> errors) {

  public static @NotNull ApiErrorResponse of(@NotNull String message) {
    return new ApiErrorResponse(message, List.of(message));
  }

  public static @NotNull ApiErrorResponse of(
      @NotNull String message, @NotNull List<String> errors) {
    return new ApiErrorResponse(message, errors);
  }
}
