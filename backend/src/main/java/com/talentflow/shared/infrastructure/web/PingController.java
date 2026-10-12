package com.talentflow.shared.infrastructure.web;

import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lightweight liveness endpoint used by docker compose and smoke tests.
 */
@RestController
@RequestMapping("/api/v1/ping")
public class PingController {

    @GetMapping
    public PingResponse ping() {
        return new PingResponse("UP", Instant.now());
    }

    public record PingResponse(String status, Instant timestamp) {
    }
}
