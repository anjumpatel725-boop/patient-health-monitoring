package com.healthcare.monitoring.dto;

import java.time.OffsetDateTime;

public record HealthDataResponse(
        Long id,
        Long patientId,
        double heartRate,
        double systolic,
        double diastolic,
        double temperature,
        double oxygenLevel,
        OffsetDateTime recordedAt
) {
}
