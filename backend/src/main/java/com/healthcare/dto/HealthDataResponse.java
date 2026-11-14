
package com.healthcare.monitoring.dto;

import java.time.LocalDateTime;

public record HealthDataResponse(
        Long id,
        Long patientId,
        double heartRate,
        double systolic,
        double diastolic,
        double temperature,
        double oxygenLevel,
        LocalDateTime recordedAt
) {
}
