package io.github.zapolyarnydev.medicalappointment.patient;

import static io.github.zapolyarnydev.medicalappointment.shared.persistence.JooqTables.Patients;

import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PatientRepository {

  private final DSLContext dsl;

  public @NotNull Optional<Patient> findById(@NotNull Long id) {
    return dsl.selectFrom(Patients.TABLE).where(Patients.ID.eq(id)).fetchOptional(this::map);
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
        .returning()
        .fetchOne(this::map);
  }

  private Patient map(Record record) {
    return new Patient(
        record.get(Patients.ID),
        record.get(Patients.FULL_NAME),
        record.get(Patients.BIRTH_DATE),
        record.get(Patients.PHONE),
        record.get(Patients.POLICY_NUMBER),
        record.get(Patients.CREATED_AT));
  }
}
