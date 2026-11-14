
package com.healthcare.monitoring.controller;

import com.healthcare.monitoring.model.HealthData;
import com.healthcare.monitoring.model.User;
import com.healthcare.monitoring.repository.HealthDataRepository;
import com.healthcare.monitoring.repository.PatientDoctorAssignmentRepository;
import com.healthcare.monitoring.repository.UserRepository;

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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthDataControllerTest {

    @Mock
    private HealthDataRepository healthRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientDoctorAssignmentRepository assignmentRepository;

    @InjectMocks
    private HealthDataController controller;

    private User patient;
    private User doctor;
    private User admin;

    @BeforeEach
    void setUp() {
        patient = createUser(3L, "patient");
        doctor = createUser(1L, "doctor");
        admin = createUser(2L, "admin");
    }

    @Test
    void patientCanViewOwnHealthData() {
        when(userRepository.findById(3L))
                .thenReturn(Optional.of(patient));

        when(healthRepository.findByPatientOrderByRecordedAtDesc(patient))
                .thenReturn(List.of());

        ResponseEntity<?> response =
                controller.getPatientHealthData(3L, authenticate(patient));

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void patientCannotViewAnotherPatientsHealthData() {
        User anotherPatient = createUser(4L, "patient");

        when(userRepository.findById(4L))
                .thenReturn(Optional.of(anotherPatient));

        ResponseEntity<?> response =
                controller.getPatientHealthData(4L, authenticate(patient));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(healthRepository, never())
                .findByPatientOrderByRecordedAtDesc(any(User.class));
    }

    @Test
    void doctorCanViewAssignedPatientsHealthData() {
        when(userRepository.findById(3L))
                .thenReturn(Optional.of(patient));

        when(assignmentRepository.existsByDoctor_IdAndPatient_Id(1L, 3L))
                .thenReturn(true);

        when(healthRepository.findByPatientOrderByRecordedAtDesc(patient))
                .thenReturn(List.of());

        ResponseEntity<?> response =
                controller.getPatientHealthData(3L, authenticate(doctor));

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void doctorCannotViewUnassignedPatientsHealthData() {
        User anotherPatient = createUser(5L, "patient");

        when(userRepository.findById(5L))
                .thenReturn(Optional.of(anotherPatient));

        when(assignmentRepository.existsByDoctor_IdAndPatient_Id(1L, 5L))
                .thenReturn(false);

        ResponseEntity<?> response =
                controller.getPatientHealthData(5L, authenticate(doctor));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void adminCanViewAllHealthData() {
        when(healthRepository.findAllByOrderByRecordedAtDesc())
                .thenReturn(List.of());

        ResponseEntity<?> response =
                controller.getAllHealthData(authenticate(admin));

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void patientCannotManuallyAddHealthData() {
        ResponseEntity<?> response =
                controller.addHealthData(
                        3L,
                        new HealthData(),
                        authenticate(patient)
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(healthRepository, never()).save(any(HealthData.class));
    }

    private User createUser(Long id, String role) {
        User user = new User();
        user.setId(id);
        user.setFullName("Test " + role);
        user.setEmail(role + "@example.com");
        user.setRole(role);
        return user;
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
