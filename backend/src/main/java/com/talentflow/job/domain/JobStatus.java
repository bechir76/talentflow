package com.talentflow.job.domain;

/**
 * Lifecycle of a job posting. Only published jobs are visible on the career page.
 */
public enum JobStatus {
    DRAFT,
    PUBLISHED,
    CLOSED;

    public boolean canChangeTo(JobStatus target) {
        return switch (this) {
            case DRAFT, CLOSED -> target == PUBLISHED;
            case PUBLISHED -> target == CLOSED;
        };
    }
}
