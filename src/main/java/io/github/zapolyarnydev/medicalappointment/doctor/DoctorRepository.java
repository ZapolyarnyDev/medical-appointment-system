package io.github.zapolyarnydev.medicalappointment.doctor;

import static io.github.zapolyarnydev.medicalappointment.shared.persistence.JooqRecordMappers.localDateTime;
import static io.github.zapolyarnydev.medicalappointment.shared.persistence.JooqTables.Doctors;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DoctorRepository {

  private final DSLContext dsl;

  public @NotNull List<Doctor> findActiveBySpecializationId(@NotNull Long specializationId) {
    return dsl.selectFrom(Doctors.TABLE)
        .where(Doctors.SPECIALIZATION_ID.eq(specializationId))
        .and(Doctors.ACTIVE.isTrue())
        .orderBy(Doctors.FULL_NAME)
        .fetch(this::map);
  }

  public @NotNull Optional<Doctor> findById(@NotNull Long id) {
    return dsl.selectFrom(Doctors.TABLE).where(Doctors.ID.eq(id)).fetchOptional(this::map);
  }

  public boolean existsById(@NotNull Long id) {
    return dsl.fetchExists(dsl.selectOne().from(Doctors.TABLE).where(Doctors.ID.eq(id)));
  }

  public @NotNull Doctor create(
      @NotNull Long specializationId, @NotNull String fullName, String cabinet) {
    return dsl.insertInto(Doctors.TABLE)
        .columns(Doctors.SPECIALIZATION_ID, Doctors.FULL_NAME, Doctors.CABINET)
        .values(specializationId, fullName, cabinet)
        .returningResult(
            Doctors.ID,
            Doctors.SPECIALIZATION_ID,
            Doctors.FULL_NAME,
            Doctors.CABINET,
            Doctors.ACTIVE,
            Doctors.CREATED_AT)
        .fetchOne(this::map);
  }

  private Doctor map(Record record) {
    return new Doctor(
        record.get(Doctors.ID),
        record.get(Doctors.SPECIALIZATION_ID),
        record.get(Doctors.FULL_NAME),
        record.get(Doctors.CABINET),
        Boolean.TRUE.equals(record.get(Doctors.ACTIVE)),
        localDateTime(record, Doctors.CREATED_AT));
  }
}
