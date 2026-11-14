
package com.healthcare.monitoring.service;

import com.healthcare.monitoring.dto.CreateDoctorRequest;
import com.healthcare.monitoring.dto.RegisterRequest;
import com.healthcare.monitoring.model.User;
import com.healthcare.monitoring.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientAssignmentService assignmentService;

    @InjectMocks
    private AuthService authService;

    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    @Test
    void createDoctorSuccessfully() {

        CreateDoctorRequest request = new CreateDoctorRequest();
        request.setFullName("  Test Doctor  ");
        request.setEmail("  TEST.DOCTOR@example.com  ");
        request.setPassword("Doctor@123");

        when(userRepository.existsByEmailIgnoreCase(
                "test.doctor@example.com"
        )).thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User doctor = authService.createDoctor(request);

        assertEquals("Test Doctor", doctor.getFullName());
        assertEquals("test.doctor@example.com", doctor.getEmail());
        assertEquals("doctor", doctor.getRole());

        assertNotEquals("Doctor@123", doctor.getPassword());
        assertTrue(encoder.matches(
                "Doctor@123",
                doctor.getPassword()
        ));

        verify(userRepository).save(any(User.class));
        verifyNoInteractions(assignmentService);
    }

    @Test
    void createDoctorRejectsDuplicateEmail() {

        CreateDoctorRequest request = new CreateDoctorRequest();
        request.setFullName("Test Doctor");
        request.setEmail("existing@example.com");
        request.setPassword("Doctor@123");

        when(userRepository.existsByEmailIgnoreCase(
                "existing@example.com"
        )).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.createDoctor(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(assignmentService);
    }

    @Test
    void registerSuccessfullyCreatesPatientAndAssignsDoctor() {

        RegisterRequest request = new RegisterRequest();
        request.setFullName("  Test Patient  ");
        request.setEmail("  TEST.PATIENT@example.com  ");
        request.setPassword("Patient@123");
        request.setRole("patient");

        when(userRepository.existsByEmailIgnoreCase(
                "test.patient@example.com"
        )).thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User savedPatient = invocation.getArgument(0);
                    savedPatient.setId(100L);
                    return savedPatient;
                });

        User patient = authService.register(request);

        assertEquals("Test Patient", patient.getFullName());
        assertEquals("test.patient@example.com", patient.getEmail());
        assertEquals("patient", patient.getRole());
        assertEquals(100L, patient.getId());

        assertNotEquals("Patient@123", patient.getPassword());
        assertTrue(encoder.matches(
                "Patient@123",
                patient.getPassword()
        ));

        verify(userRepository).save(any(User.class));
        verify(assignmentService).assignNewPatient(patient);
    }

    @Test
    void publicRegistrationRejectsDoctorRole() {

        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test Doctor");
        request.setEmail("doctor@example.com");
        request.setPassword("Doctor@123");
        request.setRole("doctor");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(assignmentService);
    }

    @Test
    void publicRegistrationRejectsAdminRole() {

        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test Admin");
        request.setEmail("admin@example.com");
        request.setPassword("Admin@123");
        request.setRole("admin");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(assignmentService);
    }

    @Test
    void patientRegistrationRejectsDuplicateEmail() {

        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test Patient");
        request.setEmail("existing@example.com");
        request.setPassword("Patient@123");
        request.setRole("patient");

        when(userRepository.existsByEmailIgnoreCase(
                "existing@example.com"
        )).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(assignmentService);
    }
}
