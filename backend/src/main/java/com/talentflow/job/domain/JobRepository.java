package com.talentflow.job.domain;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Persistence port for jobs.
 */
public interface JobRepository {

    Job save(Job job);

    Optional<Job> findById(UUID id);

    Page<Job> search(JobSearchCriteria criteria, Pageable pageable);

    Map<JobStatus, Long> countByStatus();
}
