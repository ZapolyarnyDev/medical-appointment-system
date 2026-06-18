package io.github.zapolyarnydev.medicalappointment.shared.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ReverseProxyRulesTest {

  @Test
  void publicHostDoesNotExposeInternalRoutes() throws Exception {
    String caddyfile = Files.readString(Path.of("docker/caddy/Caddyfile"));

    assertThat(caddyfile).contains("@public host public.localhost");
    assertThat(caddyfile).contains("@closed path /internal/* /admin/*");
    assertThat(caddyfile).contains("respond @closed 404");
    assertThat(caddyfile).contains("@internal host internal.localhost");
    assertThat(caddyfile).contains("@auth host auth.localhost");
    assertThat(caddyfile).contains("reverse_proxy keycloak:8080");
  }
}
