package io.github.zapolyarnydev.medicalappointment.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI medicalAppointmentOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Программный интерфейс системы записи на прием")
                .version("0.0.1")
                .description(
                    "Методы для просмотра врачей, расписания и управления записями на прием."));
  }
}
