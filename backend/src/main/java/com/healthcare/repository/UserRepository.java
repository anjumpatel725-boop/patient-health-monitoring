package com.healthcare.monitoring.repository;

import com.healthcare.monitoring.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);


    // Get only users with patient role
    List<User> findByRoleIgnoreCase(String role);
}