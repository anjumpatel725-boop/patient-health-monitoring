package com.healthcare.monitoring.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void validRegistrationRequestShouldPass() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test Patient");
        request.setEmail("test@example.com");
        request.setPassword("Password123");
        request.setRole("patient");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void invalidRegistrationRequestShouldFail() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("");
        request.setEmail("invalid-email");
        request.setPassword("123");
        request.setRole("");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void validLoginRequestShouldPass() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123");
        request.setRole("patient");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void invalidLoginEmailShouldFail() {
        LoginRequest request = new LoginRequest();
        request.setEmail("invalid-email");
        request.setPassword("Password123");
        request.setRole("patient");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void missingLoginFieldsShouldFail() {
        LoginRequest request = new LoginRequest();

        assertFalse(validator.validate(request).isEmpty());
    }
}