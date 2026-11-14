package com.healthcare.monitoring.controller;

import com.healthcare.monitoring.model.PatientDoctorAssignment;
import com.healthcare.monitoring.model.User;
import com.healthcare.monitoring.repository.PatientDoctorAssignmentRepository;
import com.healthcare.monitoring.service.PatientAssignmentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientAssignmentControllerTest {

    @Mock
    private PatientAssignmentService assignmentService;

    @Mock
    private PatientDoctorAssignmentRepository assignmentRepository;

    @InjectMocks
    private PatientAssignmentController controller;

    private User patient;
    private User doctor;
    private User anotherDoctor;
    private User admin;

    @BeforeEach
    void setUp() {
        patient = createUser(3L, "patient");
        doctor = createUser(1L, "doctor");
        anotherDoctor = createUser(2L, "doctor");
        admin = createUser(10L, "admin");
    }

    @Test
    void adminCanViewDoctorAssignments() {

        PatientDoctorAssignment assignment =
                createAssignment(patient, doctor);

        when(assignmentRepository.findByDoctor_Id(1L))
                .thenReturn(List.of(assignment));

        ResponseEntity<?> response =
                controller.getDoctorPatients(1L, authenticate(admin));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, ((List<?>) response.getBody()).size());
    }

    @Test
    void doctorCanViewOwnAssignments() {

        PatientDoctorAssignment assignment =
                createAssignment(patient, doctor);

        when(assignmentRepository.findByDoctor_Id(1L))
                .thenReturn(List.of(assignment));

        ResponseEntity<?> response =
                controller.getDoctorPatients(1L, authenticate(doctor));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, ((List<?>) response.getBody()).size());
    }

    @Test
    void doctorCannotViewAnotherDoctorsAssignments() {

        ResponseEntity<?> response =
                controller.getDoctorPatients(2L, authenticate(doctor));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(assignmentRepository, never()).findByDoctor_Id(anyLong());
    }

    @Test
    void patientCannotViewDoctorAssignments() {

        ResponseEntity<?> response =
                controller.getDoctorPatients(1L, authenticate(patient));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(assignmentRepository, never()).findByDoctor_Id(anyLong());
    }

    @Test
    void adminCanTriggerAutoAssignment() {

        Map<String, Object> result =
                Map.of("message", "Patient assignment completed");

        when(assignmentService.autoAssignPatients())
                .thenReturn(result);

        ResponseEntity<?> response =
                controller.autoAssign(authenticate(admin));

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(assignmentService, times(1)).autoAssignPatients();
    }

    @Test
    void doctorCannotTriggerAutoAssignment() {

        ResponseEntity<?> response =
                controller.autoAssign(authenticate(doctor));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(assignmentService, never()).autoAssignPatients();
    }

    private User createUser(Long id, String role) {

        User user = new User();
        user.setId(id);
        user.setFullName("Test " + role);
        user.setEmail(role + "@example.com");
        user.setRole(role);

        return user;
    }

    private PatientDoctorAssignment createAssignment(
            User patient, User doctor) {

        PatientDoctorAssignment assignment =
                new PatientDoctorAssignment();

        assignment.setPatient(patient);
        assignment.setDoctor(doctor);

        return assignment;
    }

    private Authentication authenticate(User user) {

        return new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().toUpperCase()
                        )
                )
        );
    }
}
