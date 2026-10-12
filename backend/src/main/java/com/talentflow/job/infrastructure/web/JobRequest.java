package com.talentflow.job.infrastructure.web;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import com.talentflow.job.domain.JobDetails;
import com.talentflow.job.domain.JobStatus;
import com.talentflow.job.domain.WorkMode;

import io.swagger.v3.oas.annotations.media.Schema;

public record JobRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(min = 50, max = 20000) String description,
        @NotEmpty @Size(max = JobDetails.MAX_SKILLS) List<@NotBlank @Size(max = 50) String> requiredSkills,
        @NotBlank @Size(max = 150) String location,
        @NotNull WorkMode workMode,
        @PositiveOrZero Integer salaryMin,
        @PositiveOrZero Integer salaryMax,
        @NotNull UUID departmentId,
        @Schema(description = "Initial status when creating a job (DRAFT or PUBLISHED, default DRAFT). Ignored on update.")
        JobStatus status) {

    JobDetails toDetails() {
        return new JobDetails(title, description, requiredSkills, location, workMode, salaryMin, salaryMax,
                departmentId);
    }

    JobStatus initialStatus() {
        return status == null ? JobStatus.DRAFT : status;
    }
}
