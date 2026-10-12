package com.talentflow.application.infrastructure.web;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.talentflow.application.application.ApplyForJobService;
import com.talentflow.application.domain.ApplicationStage;
import com.talentflow.application.domain.CvFile;
import com.talentflow.application.domain.JobApplication;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Applications")
public class ApplicationController {

    private final ApplyForJobService applyForJob;

    public ApplicationController(ApplyForJobService applyForJob) {
        this.applyForJob = applyForJob;
    }

    @PostMapping(path = "/api/v1/jobs/{jobId}/applications", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Applies for a published job with a PDF CV and explicit consent")
    public ResponseEntity<ApplicationResponse> apply(@PathVariable UUID jobId, @Valid @ModelAttribute ApplyForm form,
            @RequestPart("cv") MultipartFile cv) throws IOException {
        JobApplication application = applyForJob.apply(jobId, form.toCommand(CvFile.of(cv.getBytes())));
        URI location = URI.create("/api/v1/me/applications/" + application.getId());
        return ResponseEntity.created(location).body(ApplicationResponse.from(application));
    }

    public record ApplicationResponse(UUID id, UUID jobId, ApplicationStage stage, Instant appliedAt) {

        static ApplicationResponse from(JobApplication application) {
            return new ApplicationResponse(application.getId(), application.getJobId(), application.getStage(),
                    application.getAppliedAt());
        }
    }
}
