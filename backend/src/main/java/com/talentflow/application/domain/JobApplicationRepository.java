package com.talentflow.application.domain;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Persistence port for job applications.
 */
public interface JobApplicationRepository {

    JobApplication save(JobApplication application);

    boolean existsByJobIdAndCandidateId(UUID jobId, UUID candidateId);

    Page<JobApplication> findByCandidateId(UUID candidateId, Pageable pageable);

    /** Number of applications per job; jobs without applications are missing from the map. */
    Map<UUID, Long> countByJobIds(Collection<UUID> jobIds);

    long countActive();
}
