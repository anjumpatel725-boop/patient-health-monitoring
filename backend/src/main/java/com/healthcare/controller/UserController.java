
package com.healthcare.monitoring.controller;

import com.healthcare.monitoring.dto.CreateDoctorRequest;
import com.healthcare.monitoring.dto.UserResponse;
import com.healthcare.monitoring.model.User;
import com.healthcare.monitoring.repository.UserRepository;
import com.healthcare.monitoring.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final AuthService authService;

    public UserController(
            UserRepository userRepository,
            AuthService authService
    ) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole()
                ))
                .toList();

        return ResponseEntity.ok(users);
    }

    @PostMapping("/doctors")
    public ResponseEntity<?> createDoctor(
            @Valid @RequestBody CreateDoctorRequest request
    ) {

        try {
            User doctor = authService.createDoctor(request);

            UserResponse response = new UserResponse(
                    doctor.getId(),
                    doctor.getFullName(),
                    doctor.getEmail(),
                    doctor.getRole()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (ResponseStatusException exception) {

            return ResponseEntity
                    .status(exception.getStatusCode())
                    .body(Map.of(
                            "message",
                            exception.getReason() != null
                                    ? exception.getReason()
                                    : "Doctor creation failed"
                    ));
        }
    }
}
