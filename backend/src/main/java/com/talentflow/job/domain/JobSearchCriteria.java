package com.talentflow.job.domain;

import java.util.UUID;

/**
 * Optional filters for job lists. {@code null} means "no filter".
 *
 * @param text matched case-insensitively against the title and location
 */
public record JobSearchCriteria(String text, JobStatus status, UUID departmentId, WorkMode workMode) {

    public static JobSearchCriteria publishedOnly(String text, WorkMode workMode) {
        return new JobSearchCriteria(text, JobStatus.PUBLISHED, null, workMode);
    }
}
