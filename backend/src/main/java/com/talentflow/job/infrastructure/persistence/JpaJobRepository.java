package com.talentflow.job.infrastructure.persistence;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.talentflow.job.domain.Job;
import com.talentflow.job.domain.JobRepository;
import com.talentflow.job.domain.JobSearchCriteria;
import com.talentflow.job.domain.JobStatus;

@Repository
class JpaJobRepository implements JobRepository {

    private final SpringDataJobRepository jpa;

    JpaJobRepository(SpringDataJobRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Job save(Job job) {
        return jpa.save(job);
    }

    @Override
    public Optional<Job> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Page<Job> search(JobSearchCriteria criteria, Pageable pageable) {
        return jpa.findAll(JobSpecifications.matching(criteria), pageable);
    }

    @Override
    public Map<JobStatus, Long> countByStatus() {
        Map<JobStatus, Long> counts = new EnumMap<>(JobStatus.class);
        for (JobStatus status : JobStatus.values()) {
            counts.put(status, 0L);
        }
        jpa.countGroupedByStatus().forEach(row -> counts.put(row.getStatus(), row.getTotal()));
        return counts;
    }
}
