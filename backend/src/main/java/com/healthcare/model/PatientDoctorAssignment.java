package com.healthcare.monitoring.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "patient_doctor_assignments",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_assignment_patient",
            columnNames = "patient_id"
        )
    }
)
public class PatientDoctorAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    public PatientDoctorAssignment() {
    }

    public Long getId() {
        return id;
    }

    public User getPatient() {
        return patient;
    }

    public void setPatient(User patient) {
        this.patient = patient;
    }

    public User getDoctor() {
        return doctor;
    }

    public void setDoctor(User doctor) {
        this.doctor = doctor;
    }
}