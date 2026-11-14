
package com.healthcare.monitoring.service;

import com.healthcare.monitoring.model.PatientDoctorAssignment;
import com.healthcare.monitoring.model.User;
import com.healthcare.monitoring.repository.PatientDoctorAssignmentRepository;
import com.healthcare.monitoring.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientAssignmentServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientDoctorAssignmentRepository assignmentRepository;

    @InjectMocks
    private PatientAssignmentService assignmentService;

    @Test
    void assignsNewPatientToDoctorWithFewestPatients() {

        User patient = createUser(10L, "New Patient", "patient");

        User doctor1 = createUser(1L, "Doctor One", "doctor");
        User doctor2 = createUser(2L, "Doctor Two", "doctor");

        when(assignmentRepository.findByPatient_Id(10L))
                .thenReturn(Optional.empty());

        when(userRepository.findByRoleIgnoreCase("doctor"))
                .thenReturn(List.of(doctor1, doctor2));

        when(assignmentRepository.findByDoctor_Id(1L))
                .thenReturn(List.of(
                        createAssignment(patient, doctor1),
                        createAssignment(patient, doctor1)
                ));

        when(assignmentRepository.findByDoctor_Id(2L))
                .thenReturn(List.of(
                        createAssignment(patient, doctor2)
                ));

        assignmentService.assignNewPatient(patient);

        ArgumentCaptor<PatientDoctorAssignment> captor =
                ArgumentCaptor.forClass(PatientDoctorAssignment.class);

        verify(assignmentRepository).save(captor.capture());

        assertEquals(10L, captor.getValue().getPatient().getId());
        assertEquals(2L, captor.getValue().getDoctor().getId());
    }

    @Test
    void selectsDoctorWithLowerIdWhenPatientCountsAreEqual() {

        User patient = createUser(10L, "New Patient", "patient");

        User doctor1 = createUser(1L, "Doctor One", "doctor");
        User doctor2 = createUser(2L, "Doctor Two", "doctor");

        when(assignmentRepository.findByPatient_Id(10L))
                .thenReturn(Optional.empty());

        when(userRepository.findByRoleIgnoreCase("doctor"))
                .thenReturn(List.of(doctor2, doctor1));

        when(assignmentRepository.findByDoctor_Id(1L))
                .thenReturn(List.of());

        when(assignmentRepository.findByDoctor_Id(2L))
                .thenReturn(List.of());

        assignmentService.assignNewPatient(patient);

        ArgumentCaptor<PatientDoctorAssignment> captor =
                ArgumentCaptor.forClass(PatientDoctorAssignment.class);

        verify(assignmentRepository).save(captor.capture());

        assertEquals(1L, captor.getValue().getDoctor().getId());
    }

    @Test
    void doesNotCreateDuplicateAssignment() {

        User patient = createUser(10L, "Existing Patient", "patient");

        PatientDoctorAssignment existingAssignment =
                createAssignment(
                        patient,
                        createUser(1L, "Doctor One", "doctor")
                );

        when(assignmentRepository.findByPatient_Id(10L))
                .thenReturn(Optional.of(existingAssignment));

        assignmentService.assignNewPatient(patient);

        verify(userRepository, never())
                .findByRoleIgnoreCase(anyString());

        verify(assignmentRepository, never())
                .save(any(PatientDoctorAssignment.class));
    }

    @Test
    void throwsErrorWhenNoDoctorsAreAvailable() {

        User patient = createUser(10L, "New Patient", "patient");

        when(assignmentRepository.findByPatient_Id(10L))
                .thenReturn(Optional.empty());

        when(userRepository.findByRoleIgnoreCase("doctor"))
                .thenReturn(List.of());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> assignmentService.assignNewPatient(patient)
        );

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getStatusCode()
        );

        verify(assignmentRepository, never())
                .save(any(PatientDoctorAssignment.class));
    }

    private User createUser(Long id, String name, String role) {

        User user = new User();
        user.setId(id);
        user.setFullName(name);
        user.setEmail(id + "@example.com");
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
}
