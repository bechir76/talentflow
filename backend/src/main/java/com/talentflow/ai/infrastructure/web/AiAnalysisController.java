package com.talentflow.ai.infrastructure.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.talentflow.ai.application.ScreeningService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "AI screening")
public class AiAnalysisController {

    private final ScreeningService screening;

    public AiAnalysisController(ScreeningService screening) {
        this.screening = screening;
    }

    @PostMapping("/api/v1/applications/{id}/ai-analysis")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @Operation(summary = "Runs the AI screening again in the background; poll the application for the result")
    public ResponseEntity<Void> rerun(@PathVariable UUID id) {
        screening.requestScreening(id);
        return ResponseEntity.accepted().build();
    }
}
