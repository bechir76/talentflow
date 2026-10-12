package com.talentflow.application.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.talentflow.application.domain.ApplicationStage;
import com.talentflow.application.domain.JobApplication;
import com.talentflow.application.domain.JobApplicationRepository;
import com.talentflow.job.application.ApplicantCounter;

/**
 * JPA adapter for applications. Also answers the job module's applicant counts.
 */
@Repository
class JpaJobApplicationRepository implements JobApplicationRepository, ApplicantCounter {

    private static final List<ApplicationStage> FINISHED = List.of(ApplicationStage.HIRED, ApplicationStage.REJECTED);

    private final SpringDataJobApplicationRepository jpa;

    JpaJobApplicationRepository(SpringDataJobApplicationRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public JobApplication save(JobApplication application) {
        return jpa.save(application);
    }

    @Override
    public Optional<JobApplication> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public boolean existsByJobIdAndCandidateId(UUID jobId, UUID candidateId) {
        return jpa.existsByJobIdAndCandidateId(jobId, candidateId);
    }

    @Override
    public Page<JobApplication> findByCandidateId(UUID candidateId, Pageable pageable) {
        return jpa.findByCandidateId(candidateId, pageable);
    }

    @Override
    public Page<JobApplication> findByJobId(UUID jobId, Pageable pageable) {
        return jpa.findByJobId(jobId, pageable);
    }

    @Override
    public Map<UUID, Long> countByJobIds(Collection<UUID> jobIds) {
        return jpa.countByJobIds(jobIds).stream()
                .collect(Collectors.toMap(SpringDataJobApplicationRepository.JobCount::getJobId,
                        SpringDataJobApplicationRepository.JobCount::getTotal));
    }

    @Override
    public long countActive() {
        return jpa.countByStageNotIn(FINISHED);
    }
}
