package io.github.zapolyarnydev.medicalappointment.shared.persistence;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.jooq.Field;
import org.jooq.Record;

public final class JooqRecordMappers {

  private JooqRecordMappers() {}

  public static LocalDate localDate(Record record, Field<LocalDate> field) {
    Object value = record.get(field.getName());
    if (value instanceof Date date) {
      return date.toLocalDate();
    }

    return (LocalDate) value;
  }

  public static LocalDateTime localDateTime(Record record, Field<LocalDateTime> field) {
    Object value = record.get(field.getName());
    if (value instanceof Timestamp timestamp) {
      return timestamp.toLocalDateTime();
    }

    return (LocalDateTime) value;
  }
}
