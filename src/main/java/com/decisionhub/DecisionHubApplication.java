package com.decisionhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Entry point class for the DecisionHub backend application.
 */
@SpringBootApplication
@EnableJpaAuditing
public class DecisionHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(DecisionHubApplication.class, args);
    }
}
