package io.github.zapolyarnydev.medicalappointment.health;

import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  @GetMapping("/api/health")
  public @NotNull Map<String, String> health() {
    return Map.of("status", "UP");
  }
}
