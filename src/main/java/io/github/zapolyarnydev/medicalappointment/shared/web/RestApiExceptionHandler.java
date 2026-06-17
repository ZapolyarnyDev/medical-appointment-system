package io.github.zapolyarnydev.medicalappointment.shared.web;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestApiExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public @NotNull ResponseEntity<ApiErrorResponse> handleValidation(
      @NotNull MethodArgumentNotValidException exception) {
    List<String> errors =
        exception.getBindingResult().getFieldErrors().stream()
            .map(
                error ->
                    error.getDefaultMessage() == null
                        ? "Некорректное значение"
                        : error.getDefaultMessage())
            .distinct()
            .toList();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiErrorResponse.of("Некорректный запрос", errors));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public @NotNull ResponseEntity<ApiErrorResponse> handleUnreadableMessage() {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiErrorResponse.of("Некорректное тело запроса"));
  }
}
