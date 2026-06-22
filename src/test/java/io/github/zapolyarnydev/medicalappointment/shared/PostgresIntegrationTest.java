package io.github.zapolyarnydev.medicalappointment.shared;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(
    properties = {
      "app.security.public-docs-enabled=true",
      "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/.well-known/jwks.json",
      "spring.security.oauth2.client.provider.keycloak.authorization-uri=http://issuer.example/auth",
      "spring.security.oauth2.client.provider.keycloak.token-uri=http://issuer.example/token",
      "spring.security.oauth2.client.provider.keycloak.jwk-set-uri=http://issuer.example/jwks",
      "spring.security.oauth2.client.provider.keycloak.user-info-uri=http://issuer.example/userinfo",
      "spring.security.oauth2.client.provider.keycloak.user-name-attribute=sub"
    })
@Import(PostgresIntegrationTest.OAuth2TestConfig.class)
public abstract class PostgresIntegrationTest {

  private static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("medical_appointment")
          .withUsername("medical_appointment")
          .withPassword("medical_appointment");

  static {
    POSTGRES.start();
  }

  @Autowired private DSLContext dsl;

  @DynamicPropertySource
  static void configurePostgres(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @BeforeEach
  void cleanDatabase() {
    if (tableExists("appointments")) {
      dsl.execute(
          "TRUNCATE appointments, schedule_slots, patient_accounts, staff_accounts, patients, doctors, specializations RESTART IDENTITY CASCADE");
    }
  }

  private boolean tableExists(String tableName) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from("information_schema.tables")
            .where("table_schema = 'public'")
            .and("table_name = ?", tableName));
  }

  @TestConfiguration
  static class OAuth2TestConfig {

    @Bean
    ClientRegistrationRepository clientRegistrationRepository() {
      return new InMemoryClientRegistrationRepository(
          clientRegistration("public"), clientRegistration("internal"));
    }

    @Bean
    OAuth2AuthorizedClientService authorizedClientService(
        ClientRegistrationRepository clientRegistrationRepository) {
      return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Bean
    AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager(
        ClientRegistrationRepository clientRegistrationRepository,
        OAuth2AuthorizedClientService authorizedClientService) {
      return new AuthorizedClientServiceOAuth2AuthorizedClientManager(
          clientRegistrationRepository, authorizedClientService);
    }

    private ClientRegistration clientRegistration(String registrationId) {
      return ClientRegistration.withRegistrationId(registrationId)
          .clientId(registrationId)
          .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
          .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
          .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
          .authorizationUri("http://issuer.example/auth")
          .tokenUri("http://issuer.example/token")
          .jwkSetUri("http://issuer.example/jwks")
          .userInfoUri("http://issuer.example/userinfo")
          .userNameAttributeName("sub")
          .scope("openid", "profile")
          .build();
    }
  }
}
