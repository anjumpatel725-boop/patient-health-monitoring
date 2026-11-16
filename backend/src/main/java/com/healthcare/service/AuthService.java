
package com.healthcare.monitoring.service;

import com.healthcare.monitoring.dto.LoginRequest;
import com.healthcare.monitoring.dto.RegisterRequest;
import com.healthcare.monitoring.dto.CreateDoctorRequest;
import com.healthcare.monitoring.model.User;
import com.healthcare.monitoring.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PatientAssignmentService assignmentService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PatientAssignmentService assignmentService) {

        this.userRepository = userRepository;
        this.assignmentService = assignmentService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional
    public User register(RegisterRequest request) {

        // Public registration is restricted to patients.
        if (request.getRole() == null ||
                !request.getRole().trim().equalsIgnoreCase("patient")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Public registration is allowed for patients only"
            );
        }

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already registered"
            );
        }

        User user = new User();

        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("patient");

        // Save patient first to generate the database ID.
        User savedPatient = userRepository.save(user);

        // Automatically assign only this newly registered patient.
        assignmentService.assignNewPatient(savedPatient);

        return savedPatient;
    }

    public User createDoctor(CreateDoctorRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already registered"
            );
        }

        User doctor = new User();

        doctor.setFullName(request.getFullName().trim());
        doctor.setEmail(email);
        doctor.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        // Role is controlled by backend, never by frontend.
        doctor.setRole("doctor");

        return userRepository.save(doctor);
    }

    
public User login(LoginRequest request) {

    User user = userRepository
            .findByEmail(request.getEmail().trim())
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email"
            ));

    if (!passwordEncoder.matches(
            request.getPassword(),
            user.getPassword()
    )) {
        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid password"
        );
    }

    if (user.getRole() == null ||
            request.getRole() == null ||
            !user.getRole().equalsIgnoreCase(request.getRole().trim())) {

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid role selected"
        );
    }

    return user;
}

}
