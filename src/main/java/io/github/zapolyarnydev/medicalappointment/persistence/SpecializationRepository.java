package io.github.zapolyarnydev.medicalappointment.persistence;

import static io.github.zapolyarnydev.medicalappointment.persistence.JooqTables.Specializations;

import io.github.zapolyarnydev.medicalappointment.domain.Specialization;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SpecializationRepository {

  private final DSLContext dsl;

  public @NotNull List<Specialization> findAll() {
    return dsl.selectFrom(Specializations.TABLE).orderBy(Specializations.NAME).fetch(this::map);
  }

  public @NotNull Optional<Specialization> findById(@NotNull Long id) {
    return dsl.selectFrom(Specializations.TABLE)
        .where(Specializations.ID.eq(id))
        .fetchOptional(this::map);
  }

  public @NotNull Specialization create(@NotNull String name, String description) {
    return dsl.insertInto(Specializations.TABLE)
        .columns(Specializations.NAME, Specializations.DESCRIPTION)
        .values(name, description)
        .returning()
        .fetchOne(this::map);
  }

  private Specialization map(Record record) {
    return new Specialization(
        record.get(Specializations.ID),
        record.get(Specializations.NAME),
        record.get(Specializations.DESCRIPTION),
        record.get(Specializations.CREATED_AT));
  }
}
