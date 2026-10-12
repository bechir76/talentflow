package com.talentflow.job.infrastructure.web;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.talentflow.job.application.JobService;
import com.talentflow.job.infrastructure.web.JobResponses.DepartmentResponse;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/departments")
@PreAuthorize("hasAnyRole('RECRUITER', 'HIRING_MANAGER', 'ADMIN')")
@Tag(name = "Departments")
public class DepartmentController {

    private final JobService jobService;

    public DepartmentController(JobService jobService) {
        this.jobService = jobService;
    }

    /** A short reference list, so it is not paginated. */
    @GetMapping
    public List<DepartmentResponse> list() {
        return jobService.departments().stream().map(DepartmentResponse::from).toList();
    }
}
