package io.github.zapolyarnydev.medicalappointment.shared.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeploymentReadiness implements ApplicationRunner {

  private final DeploymentProperties deploymentProperties;
  private final SecurityProperties securityProperties;
  private final Environment environment;

  @Override
  public void run(ApplicationArguments args) {
    if (!isProdProfile()) {
      return;
    }

    if (securityProperties.publicDocsEnabled()) {
      throw new IllegalStateException(
          "В prod-профиле публичная документация должна быть отключена");
    }

    if (!deploymentProperties.endpoints().publicBaseUrl().startsWith("https://")
        || !deploymentProperties.endpoints().internalBaseUrl().startsWith("https://")
        || !deploymentProperties.endpoints().authBaseUrl().startsWith("https://")) {
      throw new IllegalStateException(
          "В prod-профиле все внешние адреса должны использовать HTTPS");
    }

    requireChangedSecret("DB_PASSWORD", "change-me");
  }

  private boolean isProdProfile() {
    for (String profile : environment.getActiveProfiles()) {
      if ("prod".equals(profile)) {
        return true;
      }
    }
    return false;
  }

  private void requireChangedSecret(String propertyName, String forbiddenValue) {
    String value = environment.getProperty(propertyName);
    if (forbiddenValue.equals(value)) {
      throw new IllegalStateException(propertyName + " должен быть изменен перед prod-запуском");
    }
  }
}
