package com.talentflow.shared.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables {@code @Async} (AI screening, emails). With virtual threads enabled, Spring Boot backs it
 * with a virtual-thread executor.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
