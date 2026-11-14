package com.healthcare.monitoring.repository;

import com.healthcare.monitoring.model.HealthData;
import com.healthcare.monitoring.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HealthDataRepository
        extends JpaRepository<HealthData, Long> {


    List<HealthData>
    findByPatientOrderByRecordedAtDesc(
            User patient
    );


    List<HealthData>
    findAllByOrderByRecordedAtDesc();

}