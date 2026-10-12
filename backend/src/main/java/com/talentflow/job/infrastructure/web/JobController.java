package com.talentflow.job.infrastructure.web;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentflow.job.application.JobService;
import com.talentflow.job.application.JobView;
import com.talentflow.job.domain.JobSearchCriteria;
import com.talentflow.job.domain.JobStatus;
import com.talentflow.job.infrastructure.web.JobResponses.JobResponse;
import com.talentflow.job.infrastructure.web.JobResponses.JobStatsResponse;
import com.talentflow.job.infrastructure.web.JobResponses.JobSummaryResponse;
import com.talentflow.shared.infrastructure.web.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/jobs")
@PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
@Tag(name = "Jobs", description = "Manage job postings (recruiters and admins; hiring managers can read)")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RECRUITER', 'HIRING_MANAGER', 'ADMIN')")
    @Operation(summary = "Lists jobs, optionally filtered by text, status and department")
    public PageResponse<JobSummaryResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) UUID departmentId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var criteria = new JobSearchCriteria(q, status, departmentId, null);
        return PageResponse.from(jobService.search(criteria, pageable), JobSummaryResponse::from);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HIRING_MANAGER', 'ADMIN')")
    @Operation(summary = "Counts jobs per status")
    public JobStatsResponse stats() {
        return JobStatsResponse.from(jobService.stats());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HIRING_MANAGER', 'ADMIN')")
    public JobResponse get(@PathVariable UUID id) {
        return JobResponse.from(jobService.get(id));
    }

    @PostMapping
    @Operation(summary = "Creates a job as a draft or directly published")
    public ResponseEntity<JobResponse> create(@Valid @RequestBody JobRequest request) {
        JobView created = jobService.create(request.toDetails(), request.initialStatus());
        URI location = URI.create("/api/v1/jobs/" + created.job().getId());
        return ResponseEntity.created(location).body(JobResponse.from(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Updates the content of a job; the status is not changed")
    public JobResponse update(@PathVariable UUID id, @Valid @RequestBody JobRequest request) {
        return JobResponse.from(jobService.update(id, request.toDetails()));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Publishes, closes or reopens a job")
    public JobResponse changeStatus(@PathVariable UUID id, @Valid @RequestBody JobStatusRequest request) {
        return JobResponse.from(jobService.changeStatus(id, request.status()));
    }
}
