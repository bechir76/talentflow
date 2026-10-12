package com.talentflow.pipeline.application;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.talentflow.application.domain.ApplicationStage;

/**
 * Read-only reporting queries across applications, stage history and jobs.
 */
public interface DashboardReader {

    List<JobApplicationCount> applicationsPerJob(Instant since, int limit);

    /** For every pipeline stage, how many applications ever reached it (rejected ones included). */
    Map<ApplicationStage, Long> furthestStageReached();

    /** Average days from applying to being hired, for hires since the given time; null if there were none. */
    Double averageDaysToHire(Instant since);

    List<TopMatch> topMatches(Instant since, int limit);

    record JobApplicationCount(UUID jobId, String jobTitle, long applications) {
    }

    record TopMatch(UUID applicationId, String candidateName, String jobTitle, int score) {
    }
}
