package io.github.zapolyarnydev.medicalappointment.appointment;

import static io.github.zapolyarnydev.medicalappointment.shared.persistence.JooqRecordMappers.localDateTime;
import static io.github.zapolyarnydev.medicalappointment.shared.persistence.JooqTables.Appointments;
import static io.github.zapolyarnydev.medicalappointment.shared.persistence.JooqTables.Doctors;
import static io.github.zapolyarnydev.medicalappointment.shared.persistence.JooqTables.Patients;
import static io.github.zapolyarnydev.medicalappointment.shared.persistence.JooqTables.ScheduleSlots;

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

  public @NotNull List<AppointmentDetails> findDetails() {
    return dsl.select(
            Appointments.ID,
            Appointments.PATIENT_ID,
            Appointments.SLOT_ID,
            Appointments.STATUS,
            Appointments.SOURCE,
            Appointments.CREATED_AT,
            Appointments.CANCEL_REASON,
            Patients.FULL_NAME,
            Doctors.FULL_NAME,
            Doctors.CABINET,
            ScheduleSlots.START_TIME,
            ScheduleSlots.END_TIME)
        .from(Appointments.TABLE)
        .join(Patients.TABLE)
        .on(Appointments.PATIENT_ID.eq(Patients.ID))
        .join(ScheduleSlots.TABLE)
        .on(Appointments.SLOT_ID.eq(ScheduleSlots.ID))
        .join(Doctors.TABLE)
        .on(ScheduleSlots.DOCTOR_ID.eq(Doctors.ID))
        .orderBy(ScheduleSlots.START_TIME.desc())
        .fetch(this::mapDetails);
  }

  public @NotNull List<AppointmentDetails> findDetailsByPatientId(@NotNull Long patientId) {
    return dsl.select(
            Appointments.ID,
            Appointments.PATIENT_ID,
            Appointments.SLOT_ID,
            Appointments.STATUS,
            Appointments.SOURCE,
            Appointments.CREATED_AT,
            Appointments.CANCEL_REASON,
            Patients.FULL_NAME,
            Doctors.FULL_NAME,
            Doctors.CABINET,
            ScheduleSlots.START_TIME,
            ScheduleSlots.END_TIME)
        .from(Appointments.TABLE)
        .join(Patients.TABLE)
        .on(Appointments.PATIENT_ID.eq(Patients.ID))
        .join(ScheduleSlots.TABLE)
        .on(Appointments.SLOT_ID.eq(ScheduleSlots.ID))
        .join(Doctors.TABLE)
        .on(ScheduleSlots.DOCTOR_ID.eq(Doctors.ID))
        .where(Appointments.PATIENT_ID.eq(patientId))
        .orderBy(ScheduleSlots.START_TIME.desc())
        .fetch(this::mapDetails);
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
        .returningResult(
            Appointments.ID,
            Appointments.PATIENT_ID,
            Appointments.SLOT_ID,
            Appointments.STATUS,
            Appointments.SOURCE,
            Appointments.CREATED_AT,
            Appointments.CANCEL_REASON)
        .fetchOne(this::map);
  }

  public int updateStatus(
      @NotNull Long id, @NotNull AppointmentStatus status, String cancelReason) {
    return dsl.update(Appointments.TABLE)
        .set(Appointments.STATUS, status.name())
        .set(Appointments.CANCEL_REASON, cancelReason)
        .where(Appointments.ID.eq(id))
        .execute();
  }

  private Appointment map(Record record) {
    return new Appointment(
        record.get(Appointments.ID),
        record.get(Appointments.PATIENT_ID),
        record.get(Appointments.SLOT_ID),
        AppointmentStatus.valueOf(record.get(Appointments.STATUS)),
        AppointmentSource.valueOf(record.get(Appointments.SOURCE)),
        localDateTime(record, Appointments.CREATED_AT),
        record.get(Appointments.CANCEL_REASON));
  }

  private AppointmentDetails mapDetails(Record record) {
    return new AppointmentDetails(
        record.get(Appointments.ID),
        record.get(Appointments.PATIENT_ID),
        record.get(Patients.FULL_NAME),
        record.get(Appointments.SLOT_ID),
        record.get(Doctors.FULL_NAME),
        record.get(Doctors.CABINET),
        localDateTime(record, ScheduleSlots.START_TIME),
        localDateTime(record, ScheduleSlots.END_TIME),
        AppointmentStatus.valueOf(record.get(Appointments.STATUS)),
        AppointmentSource.valueOf(record.get(Appointments.SOURCE)),
        localDateTime(record, Appointments.CREATED_AT),
        record.get(Appointments.CANCEL_REASON));
  }
}
