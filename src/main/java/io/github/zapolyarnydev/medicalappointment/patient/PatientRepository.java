package io.github.zapolyarnydev.medicalappointment.patient;

import static io.github.zapolyarnydev.medicalappointment.shared.persistence.JooqRecordMappers.localDate;
import static io.github.zapolyarnydev.medicalappointment.shared.persistence.JooqRecordMappers.localDateTime;
import static io.github.zapolyarnydev.medicalappointment.shared.persistence.JooqTables.Patients;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PatientRepository {

  private final DSLContext dsl;

  public @NotNull List<Patient> findAll() {
    return dsl.selectFrom(Patients.TABLE).orderBy(Patients.FULL_NAME).fetch(this::map);
  }

  public @NotNull List<Patient> search(@NotNull String query) {
    String pattern = "%" + query.toLowerCase().trim() + "%";
    return dsl.selectFrom(Patients.TABLE)
        .where(DSL.lower(Patients.FULL_NAME).like(pattern))
        .or(DSL.lower(Patients.PHONE).like(pattern))
        .or(DSL.lower(Patients.POLICY_NUMBER).like(pattern))
        .orderBy(Patients.FULL_NAME)
        .fetch(this::map);
  }

  public @NotNull Optional<Patient> findById(@NotNull Long id) {
    return dsl.selectFrom(Patients.TABLE).where(Patients.ID.eq(id)).fetchOptional(this::map);
  }

  public @NotNull Optional<Patient> findByPhone(@NotNull String phone) {
    return dsl.selectFrom(Patients.TABLE).where(Patients.PHONE.eq(phone)).fetchOptional(this::map);
  }

  public boolean existsById(@NotNull Long id) {
    return dsl.fetchExists(dsl.selectOne().from(Patients.TABLE).where(Patients.ID.eq(id)));
  }

  public @NotNull Patient create(
      @NotNull String fullName,
      @NotNull LocalDate birthDate,
      @NotNull String phone,
      String policyNumber) {
    return dsl.insertInto(Patients.TABLE)
        .columns(Patients.FULL_NAME, Patients.BIRTH_DATE, Patients.PHONE, Patients.POLICY_NUMBER)
        .values(fullName, birthDate, phone, policyNumber)
        .returningResult(
            Patients.ID,
            Patients.FULL_NAME,
            Patients.BIRTH_DATE,
            Patients.PHONE,
            Patients.POLICY_NUMBER,
            Patients.CREATED_AT)
        .fetchOne(this::map);
  }

  private Patient map(Record record) {
    return new Patient(
        record.get(Patients.ID),
        record.get(Patients.FULL_NAME),
        localDate(record, Patients.BIRTH_DATE),
        record.get(Patients.PHONE),
        record.get(Patients.POLICY_NUMBER),
        localDateTime(record, Patients.CREATED_AT));
  }
}
