package com.example.demo;

import com.healthcare.monitoring.HealthcareApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = HealthcareApplication.class,
    properties = "app.simulator.enabled=false"
)
class DemoApplicationTests {

    @Test
    void contextLoads() {
    }
}