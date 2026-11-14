
package com.healthcare.monitoring.controller;

import com.healthcare.monitoring.model.PatientDoctorAssignment;
import com.healthcare.monitoring.model.User;
import com.healthcare.monitoring.repository.PatientDoctorAssignmentRepository;
import com.healthcare.monitoring.service.PatientAssignmentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assignments")
public class PatientAssignmentController {

    private final PatientAssignmentService assignmentService;
    private final PatientDoctorAssignmentRepository assignmentRepository;

    public PatientAssignmentController(
            PatientAssignmentService assignmentService,
            PatientDoctorAssignmentRepository assignmentRepository) {

        this.assignmentService = assignmentService;
        this.assignmentRepository = assignmentRepository;
    }

    // Admin only: Automatically assign patients to doctors
    @PostMapping("/auto-assign")
    public ResponseEntity<?> autoAssign(Authentication authentication) {

        User currentUser = getAuthenticatedUser(authentication);

        if (currentUser == null) {
            return error(HttpStatus.UNAUTHORIZED,
                    "Authentication required");
        }

        if (!isAdmin(currentUser)) {
            return error(HttpStatus.FORBIDDEN,
                    "Only administrators can assign patients");
        }

        try {
            Object result = assignmentService.autoAssignPatients();
            return ResponseEntity.ok(result);

        } catch (IllegalStateException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to assign patients");
        }
    }

    // Admin: View any doctor's assigned patients
    // Doctor: View only their own assigned patients
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getDoctorPatients(
            @PathVariable Long doctorId,
            Authentication authentication) {

        User currentUser = getAuthenticatedUser(authentication);

        if (currentUser == null) {
            return error(HttpStatus.UNAUTHORIZED,
                    "Authentication required");
        }

        boolean authorized = isAdmin(currentUser)
                || (isDoctor(currentUser)
                && currentUser.getId().equals(doctorId));

        if (!authorized) {
            return error(HttpStatus.FORBIDDEN,
                    "You are not authorized to view these assignments");
        }

        try {
            List<Map<String, Object>> patients =
                    assignmentRepository.findByDoctor_Id(doctorId)
                            .stream()
                            .map(a -> Map.<String, Object>of(
                                    "patientId", a.getPatient().getId(),
                                    "patientName", a.getPatient().getFullName(),
                                    "email", a.getPatient().getEmail()
                            ))
                            .toList();

            return ResponseEntity.ok(patients);

        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to load assigned patients");
        }
    }
  // Patient: View their own assigned doctor
@GetMapping("/patient/me")
public ResponseEntity<?> getMyAssignedDoctor(
        Authentication authentication) {

    User currentUser = getAuthenticatedUser(authentication);

    if (currentUser == null) {
        return error(
                HttpStatus.UNAUTHORIZED,
                "Authentication required"
        );
    }

    if (currentUser.getRole() == null
            || !currentUser.getRole().equalsIgnoreCase("patient")) {
        return error(
                HttpStatus.FORBIDDEN,
                "Only patients can view their assigned doctor"
        );
    }

    return assignmentRepository
            .findByPatient_Id(currentUser.getId())
            .map(assignment -> {
                User doctor = assignment.getDoctor();

                Map<String, Object> response = Map.of(
                        "assigned", true,
                        "doctorId", doctor.getId(),
                        "doctorName", doctor.getFullName(),
                        "email", doctor.getEmail()
                );

                return ResponseEntity.ok(response);
            })
            .orElseGet(() -> ResponseEntity.ok(
                    Map.of(
                            "assigned", false,
                            "message", "No doctor assigned yet"
                    )
            ));
}
    private User getAuthenticatedUser(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof User)) {
            return null;
        }

        return (User) authentication.getPrincipal();
    }

    private boolean isAdmin(User user) {
        return user.getRole() != null
                && user.getRole().equalsIgnoreCase("admin");
    }

    private boolean isDoctor(User user) {
        return user.getRole() != null
                && user.getRole().equalsIgnoreCase("doctor");
    }

    private ResponseEntity<Map<String, String>> error(
            HttpStatus status, String message) {

        return ResponseEntity.status(status)
                .body(Map.of("message", message));
    }
}
