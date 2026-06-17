package io.github.zapolyarnydev.medicalappointment.shared;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(
    properties = {
      "app.security.public-docs-enabled=true",
      "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/.well-known/jwks.json"
    })
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
          "TRUNCATE appointments, schedule_slots, patients, doctors, specializations RESTART IDENTITY CASCADE");
    }
  }

  private boolean tableExists(String tableName) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from("information_schema.tables")
            .where("table_schema = 'public'")
            .and("table_name = ?", tableName));
  }
}
