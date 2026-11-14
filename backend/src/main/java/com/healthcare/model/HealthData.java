package com.healthcare.monitoring.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "health_data")
public class HealthData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "patient_id")
    private User patient;


    private double heartRate;

    private double systolic;

    private double diastolic;

    private double temperature;

    private double oxygenLevel;


    private LocalDateTime recordedAt;


    public HealthData() {
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public User getPatient() {
        return patient;
    }


    public void setPatient(User patient) {
        this.patient = patient;
    }


    public double getHeartRate() {
        return heartRate;
    }


    public void setHeartRate(double heartRate) {
        this.heartRate = heartRate;
    }


    public double getSystolic() {
        return systolic;
    }


    public void setSystolic(double systolic) {
        this.systolic = systolic;
    }


    public double getDiastolic() {
        return diastolic;
    }


    public void setDiastolic(double diastolic) {
        this.diastolic = diastolic;
    }


    public double getTemperature() {
        return temperature;
    }


    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }


    public double getOxygenLevel() {
        return oxygenLevel;
    }


    public void setOxygenLevel(double oxygenLevel) {
        this.oxygenLevel = oxygenLevel;
    }


    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }


    public void setRecordedAt(
            LocalDateTime recordedAt
    ) {

        this.recordedAt = recordedAt;
    }
}