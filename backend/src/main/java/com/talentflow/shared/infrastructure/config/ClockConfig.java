package com.talentflow.shared.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    /** Injected wherever "now" is needed so tests can use a fixed clock. */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
