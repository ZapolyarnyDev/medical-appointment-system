package io.github.zapolyarnydev.medicalappointment.identity;

import static io.github.zapolyarnydev.medicalappointment.shared.persistence.JooqRecordMappers.localDateTime;
import static io.github.zapolyarnydev.medicalappointment.shared.persistence.JooqTables.StaffAccounts;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StaffAccountRepository {

  private final DSLContext dsl;

  public @NotNull List<StaffAccount> findAll() {
    return dsl.selectFrom(StaffAccounts.TABLE).orderBy(StaffAccounts.USERNAME).fetch(this::map);
  }

  public @NotNull Optional<StaffAccount> findActiveByUsername(@NotNull String username) {
    return dsl.selectFrom(StaffAccounts.TABLE)
        .where(StaffAccounts.USERNAME.eq(username))
        .and(StaffAccounts.ACTIVE.isTrue())
        .fetchOptional(this::map);
  }

  public @NotNull Optional<StaffAccount> findActiveByKeycloakSubject(@NotNull String subject) {
    return dsl.selectFrom(StaffAccounts.TABLE)
        .where(StaffAccounts.KEYCLOAK_SUBJECT.eq(subject))
        .and(StaffAccounts.ACTIVE.isTrue())
        .fetchOptional(this::map);
  }

  public @NotNull StaffAccount create(
      @NotNull String username, String keycloakSubject, @NotNull StaffRole role, Long doctorId) {
    return dsl.insertInto(StaffAccounts.TABLE)
        .columns(
            StaffAccounts.USERNAME,
            StaffAccounts.KEYCLOAK_SUBJECT,
            StaffAccounts.ROLE,
            StaffAccounts.DOCTOR_ID)
        .values(username, keycloakSubject, role.name(), doctorId)
        .returningResult(
            StaffAccounts.ID,
            StaffAccounts.KEYCLOAK_SUBJECT,
            StaffAccounts.USERNAME,
            StaffAccounts.ROLE,
            StaffAccounts.DOCTOR_ID,
            StaffAccounts.ACTIVE,
            StaffAccounts.CREATED_AT)
        .fetchOne(this::map);
  }

  private StaffAccount map(Record record) {
    return new StaffAccount(
        record.get(StaffAccounts.ID),
        record.get(StaffAccounts.KEYCLOAK_SUBJECT),
        record.get(StaffAccounts.USERNAME),
        StaffRole.valueOf(record.get(StaffAccounts.ROLE)),
        record.get(StaffAccounts.DOCTOR_ID),
        Boolean.TRUE.equals(record.get(StaffAccounts.ACTIVE)),
        localDateTime(record, StaffAccounts.CREATED_AT));
  }
}
