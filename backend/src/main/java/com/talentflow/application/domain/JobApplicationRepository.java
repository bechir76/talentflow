package com.talentflow.application.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Persistence port for job applications.
 */
public interface JobApplicationRepository {

    JobApplication save(JobApplication application);

    Optional<JobApplication> findById(UUID id);

    boolean existsByJobIdAndCandidateId(UUID jobId, UUID candidateId);

    Page<JobApplication> findByCandidateId(UUID candidateId, Pageable pageable);

    List<JobApplication> findAllByCandidateId(UUID candidateId);

    Page<JobApplication> findByJobId(UUID jobId, Pageable pageable);

    /** Number of applications per job; jobs without applications are missing from the map. */
    Map<UUID, Long> countByJobIds(Collection<UUID> jobIds);

    long countActive();
}
