package io.github.zapolyarnydev.medicalappointment.persistence;

import static io.github.zapolyarnydev.medicalappointment.persistence.JooqTables.ScheduleSlots;

import io.github.zapolyarnydev.medicalappointment.domain.ScheduleSlot;
import io.github.zapolyarnydev.medicalappointment.domain.SlotStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScheduleSlotRepository {

  private final DSLContext dsl;

  public @NotNull List<ScheduleSlot> findAvailableFutureByDoctorId(
      @NotNull Long doctorId, @NotNull LocalDateTime now) {
    return dsl.selectFrom(ScheduleSlots.TABLE)
        .where(ScheduleSlots.DOCTOR_ID.eq(doctorId))
        .and(ScheduleSlots.STATUS.eq(SlotStatus.AVAILABLE.name()))
        .and(ScheduleSlots.START_TIME.gt(now))
        .orderBy(ScheduleSlots.START_TIME)
        .fetch(this::map);
  }

  public @NotNull Optional<ScheduleSlot> findById(@NotNull Long id) {
    return dsl.selectFrom(ScheduleSlots.TABLE)
        .where(ScheduleSlots.ID.eq(id))
        .fetchOptional(this::map);
  }

  public @NotNull ScheduleSlot create(
      @NotNull Long doctorId,
      @NotNull LocalDateTime startTime,
      @NotNull LocalDateTime endTime,
      @NotNull SlotStatus status) {
    return dsl.insertInto(ScheduleSlots.TABLE)
        .columns(
            ScheduleSlots.DOCTOR_ID,
            ScheduleSlots.START_TIME,
            ScheduleSlots.END_TIME,
            ScheduleSlots.STATUS)
        .values(doctorId, startTime, endTime, status.name())
        .returning()
        .fetchOne(this::map);
  }

  public int updateStatus(@NotNull Long id, @NotNull SlotStatus status) {
    return dsl.update(ScheduleSlots.TABLE)
        .set(ScheduleSlots.STATUS, status.name())
        .where(ScheduleSlots.ID.eq(id))
        .execute();
  }

  private ScheduleSlot map(Record record) {
    return new ScheduleSlot(
        record.get(ScheduleSlots.ID),
        record.get(ScheduleSlots.DOCTOR_ID),
        record.get(ScheduleSlots.START_TIME),
        record.get(ScheduleSlots.END_TIME),
        SlotStatus.valueOf(record.get(ScheduleSlots.STATUS)));
  }
}
