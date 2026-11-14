
package com.healthcare.monitoring.dto;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String role
) {
}
