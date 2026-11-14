package com.healthcare.monitoring.repository;

import com.healthcare.monitoring.model.PatientDoctorAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientDoctorAssignmentRepository
        extends JpaRepository<PatientDoctorAssignment, Long> {

    // Find assignment for a patient
    Optional<PatientDoctorAssignment> findByPatient_Id(Long patientId);

    // Find all patients assigned to a doctor
    List<PatientDoctorAssignment> findByDoctor_Id(Long doctorId);
    boolean existsByDoctor_IdAndPatient_Id(Long doctorId, Long patientId);
}