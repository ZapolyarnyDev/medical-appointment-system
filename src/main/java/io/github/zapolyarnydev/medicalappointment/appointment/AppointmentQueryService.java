package io.github.zapolyarnydev.medicalappointment.appointment;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppointmentQueryService {

  private final AppointmentRepository appointmentRepository;

  public @NotNull List<Appointment> findByPatientId(@NotNull Long patientId) {
    return appointmentRepository.findByPatientId(patientId);
  }
}
