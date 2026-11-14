package com.healthcare.monitoring.security;

import com.healthcare.monitoring.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    private static final String TEST_SECRET =
            Base64.getEncoder().encodeToString(
                    "0123456789abcdef0123456789abcdef"
                            .getBytes(StandardCharsets.UTF_8)
            );

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService, "jwtSecret", TEST_SECRET
        );

        ReflectionTestUtils.setField(
                jwtService, "jwtExpirationMs", 3600000L
        );

        user = new User();
        user.setId(7L);
        user.setFullName("Test Patient");
        user.setEmail("patient@example.com");
        user.setRole("patient");
    }

    @Test
    void shouldGenerateJwtToken() {
        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(2, token.chars()
                .filter(character -> character == '.')
                .count());
    }

    @Test
    void shouldExtractUserClaimsFromToken() {
        String token = jwtService.generateToken(user);

        assertEquals(
                "patient@example.com",
                jwtService.extractUsername(token)
        );

        assertEquals(
                7L,
                jwtService.extractUserId(token)
        );

        assertEquals(
                "patient",
                jwtService.extractRole(token)
        );
    }

    @Test
    void shouldAcceptValidToken() {
        String token = jwtService.generateToken(user);

        assertTrue(
                jwtService.isTokenValid(
                        token,
                        "patient@example.com"
                )
        );
    }

    @Test
    void shouldRejectTokenForWrongEmail() {
        String token = jwtService.generateToken(user);

        assertFalse(
                jwtService.isTokenValid(
                        token,
                        "another@example.com"
                )
        );
    }

    @Test
    void shouldRejectExpiredToken() {
        ReflectionTestUtils.setField(
                jwtService, "jwtExpirationMs", -1000L
        );

        String token = jwtService.generateToken(user);

        assertFalse(
                jwtService.isTokenValid(
                        token,
                        "patient@example.com"
                )
        );
    }

    @Test
    void shouldRejectTokenWithInvalidSignature() {
        String token = jwtService.generateToken(user);

        JwtService anotherService = new JwtService();

        String anotherSecret = Base64.getEncoder().encodeToString(
                "abcdef0123456789abcdef0123456789"
                        .getBytes(StandardCharsets.UTF_8)
        );

        ReflectionTestUtils.setField(
                anotherService, "jwtSecret", anotherSecret
        );

        assertFalse(
                anotherService.isTokenValid(
                        token,
                        "patient@example.com"
                )
        );
    }

    @Test
    void shouldRejectTokenGenerationWhenSecretIsMissing() {
        ReflectionTestUtils.setField(
                jwtService, "jwtSecret", ""
        );

        assertThrows(
                IllegalStateException.class,
                () -> jwtService.generateToken(user)
        );
    }
}