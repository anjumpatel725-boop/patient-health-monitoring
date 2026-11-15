
package com.healthcare.monitoring.service;

import com.healthcare.monitoring.model.HealthData;
import com.healthcare.monitoring.model.User;
import com.healthcare.monitoring.repository.HealthDataRepository;
import com.healthcare.monitoring.repository.UserRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@ConditionalOnProperty(
    name = "app.simulator.enabled",
    havingValue = "true",
    matchIfMissing = true
)
@Service
public class VitalSignsSimulator {
    private final UserRepository userRepository;
    private final HealthDataRepository healthDataRepository;

    private final Random random = new Random();

    // Har patient ka current scenario
    private final Map<Long, Integer> patientScenarios = new HashMap<>();

    public VitalSignsSimulator(
            UserRepository userRepository,
            HealthDataRepository healthDataRepository) {
        this.userRepository = userRepository;
        this.healthDataRepository = healthDataRepository;
    }

    // Har 30 seconds mein new reading
    @Scheduled(fixedRate = 30000)
    public void generateVitalSigns() {

        List<User> patients = userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() != null
                        && user.getRole().equalsIgnoreCase("patient"))
                .toList();

        if (patients.isEmpty()) {
            System.out.println("No patients found.");
            return;
        }

        for (User patient : patients) {
            try {
                generateReadingForPatient(patient);
            } catch (Exception e) {
                System.err.println(
                        "Failed for patient ID: " + patient.getId());
                e.printStackTrace();
            }
        }
    }

    private void generateReadingForPatient(User patient) {

        Long patientId = patient.getId();

        HealthData previous = healthDataRepository
                .findByPatientOrderByRecordedAtDesc(patient)
                .stream()
                .findFirst()
                .orElse(null);

        HealthData data = new HealthData();

        // First reading
        if (previous == null) {
            data.setHeartRate(78);
            data.setSystolic(118);
            data.setDiastolic(78);
            data.setTemperature(36.8);
            data.setOxygenLevel(98);

            patientScenarios.put(patientId, 0);

        } else {
            // Previous reading ke aas-paas gradual changes
            data.setHeartRate(
                    moveValue(previous.getHeartRate(), 2.0, 55, 150));

            data.setSystolic(
                    moveValue(previous.getSystolic(), 2.0, 90, 180));

            data.setDiastolic(
                    moveValue(previous.getDiastolic(), 1.5, 55, 115));

            data.setTemperature(
                    moveValue(previous.getTemperature(), 0.1, 35.5, 39.5));

            data.setOxygenLevel(
                    moveValue(previous.getOxygenLevel(), 0.5, 85, 100));
        }

        // Scenario kabhi-kabhi change hoga
        int scenario = patientScenarios.getOrDefault(patientId, 0);

        if (random.nextInt(20) == 0) {
            scenario = (scenario + 1) % 3;
        }

        patientScenarios.put(patientId, scenario);

        /*
         * Scenario ab readings ko direct high/low range mein
         * force nahi karega. Ye sirf target values decide karega.
         */
        if (previous == null) {
            // First reading ko stable rakho
            scenario = 0;
            patientScenarios.put(patientId, 0);
        }

        // Scenario ke hisaab se target values
        double targetHR;
        double targetSystolic;
        double targetDiastolic;
        double targetTemperature;
        double targetOxygen;

        switch (scenario) {
            case 1 -> {
                // Attention: gradually abnormal target
                targetHR = 108;
                targetSystolic = 145;
                targetDiastolic = 92;
                targetTemperature = 38.0;
                targetOxygen = 94;
            }

            case 2 -> {
                // Critical demo target
                targetHR = 130;
                targetSystolic = 180;
                targetDiastolic = 120;
                targetTemperature = 39.0;
                targetOxygen = 88;
            }

            default -> {
                // Stable target
                targetHR = 78;
                targetSystolic = 118;
                targetDiastolic = 78;
                targetTemperature = 36.8;
                targetOxygen = 98;
            }
        }

        /*
         * Readings ko target ki taraf sirf chhote steps mein move karo.
         * Isse scenario change hone par sudden jump nahi hoga.
         */
        data.setHeartRate(
                moveToward(data.getHeartRate(), targetHR, 1.0));

        data.setSystolic(
                moveToward(data.getSystolic(), targetSystolic, 1.0));

        data.setDiastolic(
                moveToward(data.getDiastolic(), targetDiastolic, 1.0));

        data.setTemperature(
                moveToward(data.getTemperature(), targetTemperature, 0.05));

        data.setOxygenLevel(
                moveToward(data.getOxygenLevel(), targetOxygen, 0.3));

        data.setPatient(patient);
        data.setRecordedAt(LocalDateTime.now(ZoneOffset.UTC));

        healthDataRepository.save(data);

        System.out.println(
                "Patient: " + patient.getFullName()
                        + " | HR: " + data.getHeartRate()
                        + " | BP: " + data.getSystolic()
                        + "/" + data.getDiastolic()
                        + " | Temp: " + data.getTemperature()
                        + " | SpO2: " + data.getOxygenLevel()
                        + " | Scenario: " + scenario
        );
    }

    // Previous value ke paas chhota random change
    private double moveValue(
            double previous,
            double maxChange,
            double minimum,
            double maximum) {

        double change =
                (random.nextDouble() * 2.0 - 1.0) * maxChange;

        double result = previous + change;

        result = Math.max(minimum, Math.min(maximum, result));

        return Math.round(result * 10.0) / 10.0;
    }

    // Target ki taraf limited step
    private double moveToward(
            double current,
            double target,
            double maxStep) {

        double difference = target - current;

        // Target tak ek baar mein maximum maxStep hi move karo
        double step = Math.max(
                -maxStep,
                Math.min(maxStep, difference)
        );

        // Bahut chhota natural variation
        double noise = (random.nextDouble() - 0.5) * maxStep * 0.2;

        double result = current + step + noise;

        return Math.round(result * 10.0) / 10.0;
    }
}

