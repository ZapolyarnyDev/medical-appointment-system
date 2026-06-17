package io.github.zapolyarnydev.medicalappointment.identity;

import static io.github.zapolyarnydev.medicalappointment.shared.persistence.JooqRecordMappers.localDateTime;
import static io.github.zapolyarnydev.medicalappointment.shared.persistence.JooqTables.PatientAccounts;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PatientAccountRepository {

  private final DSLContext dsl;

  public @NotNull List<PatientAccount> findAll() {
    return dsl.selectFrom(PatientAccounts.TABLE).orderBy(PatientAccounts.USERNAME).fetch(this::map);
  }

  public @NotNull Optional<PatientAccount> findActiveByUsername(@NotNull String username) {
    return dsl.selectFrom(PatientAccounts.TABLE)
        .where(PatientAccounts.USERNAME.eq(username))
        .and(PatientAccounts.ACTIVE.isTrue())
        .fetchOptional(this::map);
  }

  public @NotNull Optional<PatientAccount> findActiveByKeycloakSubject(@NotNull String subject) {
    return dsl.selectFrom(PatientAccounts.TABLE)
        .where(PatientAccounts.KEYCLOAK_SUBJECT.eq(subject))
        .and(PatientAccounts.ACTIVE.isTrue())
        .fetchOptional(this::map);
  }

  public @NotNull PatientAccount createForUsername(
      @NotNull String username, @NotNull Long patientId) {
    return dsl.insertInto(PatientAccounts.TABLE)
        .columns(PatientAccounts.USERNAME, PatientAccounts.PATIENT_ID)
        .values(username, patientId)
        .returningResult(
            PatientAccounts.ID,
            PatientAccounts.KEYCLOAK_SUBJECT,
            PatientAccounts.USERNAME,
            PatientAccounts.PATIENT_ID,
            PatientAccounts.ACTIVE,
            PatientAccounts.CREATED_AT)
        .fetchOne(this::map);
  }

  public @NotNull PatientAccount create(
      @NotNull String username, String keycloakSubject, @NotNull Long patientId) {
    return dsl.insertInto(PatientAccounts.TABLE)
        .columns(
            PatientAccounts.USERNAME, PatientAccounts.KEYCLOAK_SUBJECT, PatientAccounts.PATIENT_ID)
        .values(username, keycloakSubject, patientId)
        .returningResult(
            PatientAccounts.ID,
            PatientAccounts.KEYCLOAK_SUBJECT,
            PatientAccounts.USERNAME,
            PatientAccounts.PATIENT_ID,
            PatientAccounts.ACTIVE,
            PatientAccounts.CREATED_AT)
        .fetchOne(this::map);
  }

  private PatientAccount map(Record record) {
    return new PatientAccount(
        record.get(PatientAccounts.ID),
        record.get(PatientAccounts.KEYCLOAK_SUBJECT),
        record.get(PatientAccounts.USERNAME),
        record.get(PatientAccounts.PATIENT_ID),
        Boolean.TRUE.equals(record.get(PatientAccounts.ACTIVE)),
        localDateTime(record, PatientAccounts.CREATED_AT));
  }
}
