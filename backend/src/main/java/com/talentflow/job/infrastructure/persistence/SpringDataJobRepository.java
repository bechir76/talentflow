package com.talentflow.job.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.talentflow.job.domain.Job;
import com.talentflow.job.domain.JobStatus;

interface SpringDataJobRepository extends JpaRepository<Job, UUID>, JpaSpecificationExecutor<Job> {

    @Query("select j.status as status, count(j) as total from Job j group by j.status")
    List<StatusCount> countGroupedByStatus();

    interface StatusCount {
        JobStatus getStatus();

        long getTotal();
    }
}
