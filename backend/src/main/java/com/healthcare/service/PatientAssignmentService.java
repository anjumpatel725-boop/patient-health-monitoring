
package com.healthcare.monitoring.service;

import com.healthcare.monitoring.model.PatientDoctorAssignment;
import com.healthcare.monitoring.model.User;
import com.healthcare.monitoring.repository.PatientDoctorAssignmentRepository;
import com.healthcare.monitoring.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatientAssignmentService {

    private final UserRepository userRepository;
    private final PatientDoctorAssignmentRepository assignmentRepository;

    public PatientAssignmentService(
            UserRepository userRepository,
            PatientDoctorAssignmentRepository assignmentRepository) {

        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional
    public void assignNewPatient(User patient) {

        if (patient == null || patient.getId() == null) {
            throw new IllegalArgumentException(
                    "A saved patient is required for assignment"
            );
        }

        // Do not assign the same patient twice
        if (assignmentRepository
                .findByPatient_Id(patient.getId())
                .isPresent()) {
            return;
        }

        List<User> doctors =
                userRepository.findByRoleIgnoreCase("doctor");

        if (doctors.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No doctors are available for patient assignment"
            );
        }

        // Select the doctor with the fewest assigned patients
        User selectedDoctor = doctors.stream()
                .min(Comparator
                        .comparingInt((User doctor) ->
                                assignmentRepository
                                        .findByDoctor_Id(doctor.getId())
                                        .size())
                        .thenComparing(User::getId))
                .orElseThrow();

        PatientDoctorAssignment assignment =
                new PatientDoctorAssignment();

        assignment.setPatient(patient);
        assignment.setDoctor(selectedDoctor);

        assignmentRepository.save(assignment);
    }

    // Existing Admin auto-assignment method
    @Transactional
    public Map<String, Object> autoAssignPatients() {

        List<User> allUsers = userRepository.findAll();

        List<User> doctors = allUsers.stream()
                .filter(u -> u.getRole() != null
                        && "doctor".equalsIgnoreCase(u.getRole()))
                .sorted(Comparator.comparing(User::getId))
                .toList();

        List<User> patients = allUsers.stream()
                .filter(u -> u.getRole() != null
                        && "patient".equalsIgnoreCase(u.getRole()))
                .sorted(Comparator.comparing(User::getId))
                .toList();

        if (doctors.isEmpty()) {
            throw new IllegalStateException("No doctors registered");
        }

        Map<Long, Integer> counts = new HashMap<>();

        for (User doctor : doctors) {
            counts.put(
                    doctor.getId(),
                    assignmentRepository
                            .findByDoctor_Id(doctor.getId()).size()
            );
        }

        int assignedNow = 0;

        for (User patient : patients) {

            boolean alreadyAssigned = assignmentRepository
                    .findByPatient_Id(patient.getId())
                    .isPresent();

            if (alreadyAssigned) {
                continue;
            }

            User selectedDoctor = doctors.stream()
                    .min(Comparator
                            .comparingInt((User d) -> counts.get(d.getId()))
                            .thenComparing(User::getId))
                    .orElseThrow();

            PatientDoctorAssignment assignment =
                    new PatientDoctorAssignment();

            assignment.setPatient(patient);
            assignment.setDoctor(selectedDoctor);

            assignmentRepository.save(assignment);

            counts.put(
                    selectedDoctor.getId(),
                    counts.get(selectedDoctor.getId()) + 1
            );

            assignedNow++;
        }

        Map<String, Object> result = new HashMap<>();

        result.put("message", "Patient assignment completed");
        result.put("newAssignments", assignedNow);
        result.put("totalPatients", patients.size());
        result.put("doctorWiseCounts", counts);

        return result;
    }
}
