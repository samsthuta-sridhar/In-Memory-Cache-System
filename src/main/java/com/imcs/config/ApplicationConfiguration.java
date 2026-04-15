package com.imcs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class ApplicationConfiguration {
    // Enables @Scheduled annotation for TTLManager
    // Spring context bridge for JavaFX integration
}