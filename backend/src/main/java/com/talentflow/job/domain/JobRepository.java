package com.talentflow.job.domain;

import java.util.Collection;
import java.util.List;
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

    List<Job> findAllById(Collection<UUID> ids);

    Page<Job> search(JobSearchCriteria criteria, Pageable pageable);

    Map<JobStatus, Long> countByStatus();
}
