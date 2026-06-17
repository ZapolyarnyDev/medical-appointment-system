package io.github.zapolyarnydev.medicalappointment.appointment;

import org.jetbrains.annotations.Nullable;

public record PatientBookAppointmentRequest(@Nullable Long doctorId, @Nullable Long slotId) {}
