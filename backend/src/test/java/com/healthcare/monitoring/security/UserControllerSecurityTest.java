
package com.healthcare.monitoring.security;

import com.healthcare.monitoring.HealthcareApplication;
import com.healthcare.monitoring.dto.CreateDoctorRequest;
import com.healthcare.monitoring.model.User;
import com.healthcare.monitoring.repository.UserRepository;
import com.healthcare.monitoring.service.AuthService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = HealthcareApplication.class)
@AutoConfigureMockMvc
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthService authService;

    private static final String DOCTOR_REQUEST = """
            {
                "fullName": "Test Doctor",
                "email": "security.doctor@example.com",
                "password": "Doctor@123"
            }
            """;

    @Test
    void adminCanAccessDoctorCreationEndpoint() throws Exception {

        User doctor = new User();
        doctor.setId(100L);
        doctor.setFullName("Test Doctor");
        doctor.setEmail("security.doctor@example.com");
        doctor.setRole("doctor");

        when(authService.createDoctor(any(CreateDoctorRequest.class)))
                .thenReturn(doctor);

        mockMvc.perform(post("/api/users/doctors")
                .with(user("admin@example.com").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(DOCTOR_REQUEST))
                .andExpect(status().isCreated());
    }

    @Test
    void patientCannotAccessDoctorCreationEndpoint() throws Exception {

        mockMvc.perform(post("/api/users/doctors")
                .with(user("patient@example.com").roles("PATIENT"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(DOCTOR_REQUEST))
                .andExpect(status().isForbidden());

        verifyNoInteractions(authService);
    }

    @Test
    void doctorCannotAccessDoctorCreationEndpoint() throws Exception {

        mockMvc.perform(post("/api/users/doctors")
                .with(user("doctor@example.com").roles("DOCTOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(DOCTOR_REQUEST))
                .andExpect(status().isForbidden());

        verifyNoInteractions(authService);
    }

    @Test
    void unauthenticatedUserCannotAccessDoctorCreationEndpoint()
            throws Exception {

        mockMvc.perform(post("/api/users/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(DOCTOR_REQUEST))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(authService);
    }
}
