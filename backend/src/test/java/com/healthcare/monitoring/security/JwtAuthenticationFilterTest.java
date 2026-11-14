package com.healthcare.monitoring.security;

import com.healthcare.monitoring.model.User;
import com.healthcare.monitoring.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private User patient;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        patient = new User();
        patient.setId(7L);
        patient.setFullName("Test Patient");
        patient.setEmail("patient@example.com");
        patient.setRole("patient");
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validTokenAuthenticatesUser() throws Exception {
        MockHttpServletRequest request = requestWithToken("valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractUsername("valid-token"))
                .thenReturn("patient@example.com");
        when(userRepository.findByEmail("patient@example.com"))
                .thenReturn(Optional.of(patient));
        when(jwtService.isTokenValid("valid-token", "patient@example.com"))
                .thenReturn(true);
        when(jwtService.extractUserId("valid-token"))
                .thenReturn(7L);

        filter.doFilter(request, response, chain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals(patient, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT")));
        assertNotNull(chain.getRequest());
    }

    @Test
    void missingTokenDoesNotAuthenticateUser() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
        verifyNoInteractions(jwtService, userRepository);
    }

    @Test
    void invalidTokenDoesNotAuthenticateUser() throws Exception {
        MockHttpServletRequest request = requestWithToken("invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractUsername("invalid-token"))
                .thenThrow(new IllegalArgumentException("Invalid token"));

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void expiredTokenDoesNotAuthenticateUser() throws Exception {
        MockHttpServletRequest request = requestWithToken("expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractUsername("expired-token"))
                .thenReturn("patient@example.com");
        when(userRepository.findByEmail("patient@example.com"))
                .thenReturn(Optional.of(patient));
        when(jwtService.isTokenValid("expired-token", "patient@example.com"))
                .thenReturn(false);

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
    }

    @Test
    void mismatchedUserIdDoesNotAuthenticateUser() throws Exception {
        MockHttpServletRequest request = requestWithToken("mismatch-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractUsername("mismatch-token"))
                .thenReturn("patient@example.com");
        when(userRepository.findByEmail("patient@example.com"))
                .thenReturn(Optional.of(patient));
        when(jwtService.isTokenValid("mismatch-token", "patient@example.com"))
                .thenReturn(true);
        when(jwtService.extractUserId("mismatch-token"))
                .thenReturn(999L);

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
    }

    @Test
    void unknownDatabaseUserDoesNotAuthenticate() throws Exception {
        MockHttpServletRequest request = requestWithToken("unknown-user-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractUsername("unknown-user-token"))
                .thenReturn("unknown@example.com");
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());

        verify(jwtService, never()).isTokenValid(anyString(), anyString());
    }

    private MockHttpServletRequest requestWithToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}