package io.github.zapolyarnydev.medicalappointment.shared.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

public final class JooqTables {

  private JooqTables() {}

  public static final class Specializations {
    public static final Table<?> TABLE = DSL.table(DSL.name("specializations"));
    public static final Field<Long> ID = field("id", Long.class);
    public static final Field<String> NAME = field("name", String.class);
    public static final Field<String> DESCRIPTION = field("description", String.class);
    public static final Field<LocalDateTime> CREATED_AT = field("created_at", LocalDateTime.class);

    private Specializations() {}

    private static <T> Field<T> field(String name, Class<T> type) {
      return DSL.field(DSL.name("specializations", name), type);
    }
  }

  public static final class Doctors {
    public static final Table<?> TABLE = DSL.table(DSL.name("doctors"));
    public static final Field<Long> ID = field("id", Long.class);
    public static final Field<Long> SPECIALIZATION_ID = field("specialization_id", Long.class);
    public static final Field<String> FULL_NAME = field("full_name", String.class);
    public static final Field<String> CABINET = field("cabinet", String.class);
    public static final Field<Boolean> ACTIVE = field("active", Boolean.class);
    public static final Field<LocalDateTime> CREATED_AT = field("created_at", LocalDateTime.class);

    private Doctors() {}

    private static <T> Field<T> field(String name, Class<T> type) {
      return DSL.field(DSL.name("doctors", name), type);
    }
  }

  public static final class Patients {
    public static final Table<?> TABLE = DSL.table(DSL.name("patients"));
    public static final Field<Long> ID = field("id", Long.class);
    public static final Field<String> FULL_NAME = field("full_name", String.class);
    public static final Field<LocalDate> BIRTH_DATE = field("birth_date", LocalDate.class);
    public static final Field<String> PHONE = field("phone", String.class);
    public static final Field<String> POLICY_NUMBER = field("policy_number", String.class);
    public static final Field<LocalDateTime> CREATED_AT = field("created_at", LocalDateTime.class);

    private Patients() {}

    private static <T> Field<T> field(String name, Class<T> type) {
      return DSL.field(DSL.name("patients", name), type);
    }
  }

  public static final class ScheduleSlots {
    public static final Table<?> TABLE = DSL.table(DSL.name("schedule_slots"));
    public static final Field<Long> ID = field("id", Long.class);
    public static final Field<Long> DOCTOR_ID = field("doctor_id", Long.class);
    public static final Field<LocalDateTime> START_TIME = field("start_time", LocalDateTime.class);
    public static final Field<LocalDateTime> END_TIME = field("end_time", LocalDateTime.class);
    public static final Field<String> STATUS = field("status", String.class);

    private ScheduleSlots() {}

    private static <T> Field<T> field(String name, Class<T> type) {
      return DSL.field(DSL.name("schedule_slots", name), type);
    }
  }

  public static final class Appointments {
    public static final Table<?> TABLE = DSL.table(DSL.name("appointments"));
    public static final Field<Long> ID = field("id", Long.class);
    public static final Field<Long> PATIENT_ID = field("patient_id", Long.class);
    public static final Field<Long> SLOT_ID = field("slot_id", Long.class);
    public static final Field<String> STATUS = field("status", String.class);
    public static final Field<String> SOURCE = field("source", String.class);
    public static final Field<LocalDateTime> CREATED_AT = field("created_at", LocalDateTime.class);
    public static final Field<String> CANCEL_REASON = field("cancel_reason", String.class);

    private Appointments() {}

    private static <T> Field<T> field(String name, Class<T> type) {
      return DSL.field(DSL.name("appointments", name), type);
    }
  }

  public static final class PatientAccounts {
    public static final Table<?> TABLE = DSL.table(DSL.name("patient_accounts"));
    public static final Field<Long> ID = field("id", Long.class);
    public static final Field<String> KEYCLOAK_SUBJECT = field("keycloak_subject", String.class);
    public static final Field<String> USERNAME = field("username", String.class);
    public static final Field<Long> PATIENT_ID = field("patient_id", Long.class);
    public static final Field<Boolean> ACTIVE = field("active", Boolean.class);
    public static final Field<LocalDateTime> CREATED_AT = field("created_at", LocalDateTime.class);

    private PatientAccounts() {}

    private static <T> Field<T> field(String name, Class<T> type) {
      return DSL.field(DSL.name("patient_accounts", name), type);
    }
  }

  public static final class StaffAccounts {
    public static final Table<?> TABLE = DSL.table(DSL.name("staff_accounts"));
    public static final Field<Long> ID = field("id", Long.class);
    public static final Field<String> KEYCLOAK_SUBJECT = field("keycloak_subject", String.class);
    public static final Field<String> USERNAME = field("username", String.class);
    public static final Field<String> ROLE = field("role", String.class);
    public static final Field<Long> DOCTOR_ID = field("doctor_id", Long.class);
    public static final Field<Boolean> ACTIVE = field("active", Boolean.class);
    public static final Field<LocalDateTime> CREATED_AT = field("created_at", LocalDateTime.class);

    private StaffAccounts() {}

    private static <T> Field<T> field(String name, Class<T> type) {
      return DSL.field(DSL.name("staff_accounts", name), type);
    }
  }
}
