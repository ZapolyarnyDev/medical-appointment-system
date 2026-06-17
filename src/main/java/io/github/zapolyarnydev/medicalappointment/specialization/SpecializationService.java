package io.github.zapolyarnydev.medicalappointment.specialization;

import io.github.zapolyarnydev.medicalappointment.doctor.Doctor;
import io.github.zapolyarnydev.medicalappointment.doctor.DoctorRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpecializationService {

  private final SpecializationRepository specializationRepository;
  private final DoctorRepository doctorRepository;

  public @NotNull List<Specialization> findSpecializations() {
    return specializationRepository.findAll();
  }

  public @NotNull List<Doctor> findActiveDoctorsBySpecialization(@NotNull Long specializationId) {
    return doctorRepository.findActiveBySpecializationId(specializationId);
  }
}
