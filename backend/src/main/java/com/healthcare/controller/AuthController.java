
package com.healthcare.monitoring.controller;

import com.healthcare.monitoring.dto.LoginRequest;
import com.healthcare.monitoring.dto.RegisterRequest;
import com.healthcare.monitoring.model.User;
import com.healthcare.monitoring.service.AuthService;
import com.healthcare.monitoring.security.JwtService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(
            AuthService authService,
            JwtService jwtService
    ) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        try {
            User user = authService.register(request);

            Map<String, Object> response = new HashMap<>();

            response.put("message", "Registration successful");
            response.put("userId", user.getId());
            response.put("name", user.getFullName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());

            return ResponseEntity.ok(response);

        } catch (ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(Map.of(
                            "message",
                            e.getReason() != null
                                    ? e.getReason()
                                    : "Request could not be completed"
                    ));

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message",
                            "Registration failed due to a server error"
                    ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request
    ) {
        try {
            User user = authService.login(request);

            String token = jwtService.generateToken(user);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Login successful",
                            "userId", user.getId(),
                            "name", user.getFullName(),
                            "email", user.getEmail(),
                            "role", user.getRole(),
                            "token", token
                    )
            );

        } catch (ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(Map.of(
                            "message",
                            e.getReason() != null
                                    ? e.getReason()
                                    : "Login failed"
                    ));

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message",
                            "Login failed due to a server error"
                    ));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            Authentication authentication
    ) {

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof User user)) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        return ResponseEntity.ok(
                Map.of(
                        "userId", user.getId(),
                        "name", user.getFullName(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                )
        );
    }
}
