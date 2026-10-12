package com.talentflow.job.infrastructure.web;

import jakarta.validation.constraints.NotNull;

import com.talentflow.job.domain.JobStatus;

public record JobStatusRequest(@NotNull JobStatus status) {
}
