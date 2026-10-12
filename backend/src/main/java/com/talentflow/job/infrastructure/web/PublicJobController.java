package com.talentflow.job.infrastructure.web;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentflow.job.application.JobService;
import com.talentflow.job.domain.WorkMode;
import com.talentflow.job.infrastructure.web.JobResponses.PublicJobResponse;
import com.talentflow.job.infrastructure.web.JobResponses.PublicJobSummaryResponse;
import com.talentflow.shared.infrastructure.web.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/public/jobs")
@SecurityRequirements
@Tag(name = "Career page", description = "Published jobs, no sign-in required")
public class PublicJobController {

    private final JobService jobService;

    public PublicJobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    @Operation(summary = "Lists published jobs")
    public PageResponse<PublicJobSummaryResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) WorkMode workMode,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.from(jobService.searchPublished(q, workMode, pageable), PublicJobSummaryResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Returns a published job")
    public PublicJobResponse get(@PathVariable UUID id) {
        return PublicJobResponse.from(jobService.getPublished(id));
    }
}
