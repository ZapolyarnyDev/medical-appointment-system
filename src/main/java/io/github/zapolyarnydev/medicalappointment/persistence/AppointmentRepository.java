package io.github.zapolyarnydev.medicalappointment.persistence;

import static io.github.zapolyarnydev.medicalappointment.persistence.JooqTables.Appointments;

import io.github.zapolyarnydev.medicalappointment.domain.Appointment;
import io.github.zapolyarnydev.medicalappointment.domain.AppointmentSource;
import io.github.zapolyarnydev.medicalappointment.domain.AppointmentStatus;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AppointmentRepository {

  private final DSLContext dsl;

  public @NotNull Optional<Appointment> findById(@NotNull Long id) {
    return dsl.selectFrom(Appointments.TABLE)
        .where(Appointments.ID.eq(id))
        .fetchOptional(this::map);
  }

  public @NotNull List<Appointment> findByPatientId(@NotNull Long patientId) {
    return dsl.selectFrom(Appointments.TABLE)
        .where(Appointments.PATIENT_ID.eq(patientId))
        .orderBy(Appointments.CREATED_AT.desc())
        .fetch(this::map);
  }

  public boolean existsBySlotId(@NotNull Long slotId) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from(Appointments.TABLE)
            .where(Appointments.SLOT_ID.eq(slotId))
            .and(Appointments.STATUS.ne(AppointmentStatus.CANCELLED.name())));
  }

  public @NotNull Appointment create(
      @NotNull Long patientId, @NotNull Long slotId, @NotNull AppointmentSource source) {
    return dsl.insertInto(Appointments.TABLE)
        .columns(
            Appointments.PATIENT_ID, Appointments.SLOT_ID, Appointments.STATUS, Appointments.SOURCE)
        .values(patientId, slotId, AppointmentStatus.CREATED.name(), source.name())
        .returning()
        .fetchOne(this::map);
  }

  private Appointment map(Record record) {
    return new Appointment(
        record.get(Appointments.ID),
        record.get(Appointments.PATIENT_ID),
        record.get(Appointments.SLOT_ID),
        AppointmentStatus.valueOf(record.get(Appointments.STATUS)),
        AppointmentSource.valueOf(record.get(Appointments.SOURCE)),
        record.get(Appointments.CREATED_AT),
        record.get(Appointments.CANCEL_REASON));
  }
}
