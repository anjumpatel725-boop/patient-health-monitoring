
package com.healthcare.monitoring.controller;

import com.healthcare.monitoring.dto.HealthDataResponse;
import com.healthcare.monitoring.model.HealthData;
import com.healthcare.monitoring.model.User;
import com.healthcare.monitoring.repository.HealthDataRepository;
import com.healthcare.monitoring.repository.PatientDoctorAssignmentRepository;
import com.healthcare.monitoring.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/health")
public class HealthDataController {

    private final HealthDataRepository healthRepository;
    private final UserRepository userRepository;
    private final PatientDoctorAssignmentRepository assignmentRepository;

    public HealthDataController(
            HealthDataRepository healthRepository,
            UserRepository userRepository,
            PatientDoctorAssignmentRepository assignmentRepository) {

        this.healthRepository = healthRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    // Admin only: Get all health readings
    @GetMapping("/all")
    public ResponseEntity<?> getAllHealthData(
            Authentication authentication) {

        User currentUser = getAuthenticatedUser(authentication);

        if (currentUser == null) {
            return error(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        if (!isAdmin(currentUser)) {
            return error(HttpStatus.FORBIDDEN,
                    "Only administrators can view all health readings");
        }

        List<HealthDataResponse> readings =
                healthRepository.findAllByOrderByRecordedAtDesc()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(readings);
    }

    // Admin: any patient
    // Patient: own readings only
    // Doctor: assigned patients only
    @GetMapping("/{patientId}")
    public ResponseEntity<?> getPatientHealthData(
            @PathVariable Long patientId,
            Authentication authentication) {

        User currentUser = getAuthenticatedUser(authentication);

        if (currentUser == null) {
            return error(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        Optional<User> optionalPatient = userRepository.findById(patientId);

        if (optionalPatient.isEmpty()) {
            return error(HttpStatus.NOT_FOUND, "Patient not found");
        }

        User patient = optionalPatient.get();

        if (patient.getRole() == null ||
                !patient.getRole().equalsIgnoreCase("patient")) {
            return error(HttpStatus.BAD_REQUEST, "User is not a patient");
        }

        boolean authorized = false;

        if (isAdmin(currentUser)) {
            authorized = true;
        } else if (isPatient(currentUser)) {
            authorized = currentUser.getId().equals(patientId);
        } else if (isDoctor(currentUser)) {
            authorized = assignmentRepository
                    .existsByDoctor_IdAndPatient_Id(
                            currentUser.getId(), patientId);
        }

        if (!authorized) {
            return error(HttpStatus.FORBIDDEN,
                    "You are not authorized to view this patient's health data");
        }

        List<HealthDataResponse> readings =
                healthRepository.findByPatientOrderByRecordedAtDesc(patient)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(readings);
    }

    // Admin only: Add a health reading
    @PostMapping("/{patientId}")
    public ResponseEntity<?> addHealthData(
            @PathVariable Long patientId,
            @RequestBody HealthData data,
            Authentication authentication) {

        User currentUser = getAuthenticatedUser(authentication);

        if (currentUser == null) {
            return error(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        if (!isAdmin(currentUser)) {
            return error(HttpStatus.FORBIDDEN,
                    "Only administrators can manually add health readings");
        }

        Optional<User> optionalPatient = userRepository.findById(patientId);

        if (optionalPatient.isEmpty()) {
            return error(HttpStatus.NOT_FOUND, "Patient not found");
        }

        User patient = optionalPatient.get();

        if (patient.getRole() == null ||
                !patient.getRole().equalsIgnoreCase("patient")) {
            return error(HttpStatus.BAD_REQUEST,
                    "Health data can only be added for patients");
        }

        data.setPatient(patient);
        data.setRecordedAt(LocalDateTime.now());

        HealthData saved = healthRepository.save(data);

        return ResponseEntity.ok(toResponse(saved));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof User)) {
            return null;
        }

        return (User) authentication.getPrincipal();
    }

    private boolean isAdmin(User user) {
        return user.getRole() != null &&
                user.getRole().equalsIgnoreCase("admin");
    }

    private boolean isPatient(User user) {
        return user.getRole() != null &&
                user.getRole().equalsIgnoreCase("patient");
    }

    private boolean isDoctor(User user) {
        return user.getRole() != null &&
                user.getRole().equalsIgnoreCase("doctor");
    }

    private HealthDataResponse toResponse(HealthData data) {
        return new HealthDataResponse(
                data.getId(),
                data.getPatient().getId(),
                data.getHeartRate(),
                data.getSystolic(),
                data.getDiastolic(),
                data.getTemperature(),
                data.getOxygenLevel(),
                data.getRecordedAt()
        );
    }

    private ResponseEntity<Map<String, String>> error(
            HttpStatus status, String message) {

        return ResponseEntity.status(status)
                .body(Map.of("message", message));
    }
}
