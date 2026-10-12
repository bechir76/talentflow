package com.talentflow.application.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.talentflow.application.domain.ApplicationStage;
import com.talentflow.application.domain.JobApplication;

interface SpringDataJobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    boolean existsByJobIdAndCandidateId(UUID jobId, UUID candidateId);

    Page<JobApplication> findByCandidateId(UUID candidateId, Pageable pageable);

    Page<JobApplication> findByJobId(UUID jobId, Pageable pageable);

    List<JobApplication> findAllByCandidateIdOrderByAppliedAtDesc(UUID candidateId);

    @Query("""
            select a.jobId as jobId, count(a) as total from JobApplication a
            where a.jobId in :jobIds group by a.jobId""")
    List<JobCount> countByJobIds(@Param("jobIds") Collection<UUID> jobIds);

    long countByStageNotIn(Collection<ApplicationStage> stages);

    interface JobCount {
        UUID getJobId();

        long getTotal();
    }
}
