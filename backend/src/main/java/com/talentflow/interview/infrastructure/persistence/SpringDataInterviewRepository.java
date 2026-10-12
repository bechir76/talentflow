package com.talentflow.interview.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.talentflow.interview.domain.Interview;

interface SpringDataInterviewRepository extends JpaRepository<Interview, UUID> {

    List<Interview> findByApplicationIdOrderByScheduledAt(UUID applicationId);

    @Query(value = """
            SELECT * FROM interview WHERE scheduled_at >= :from AND scheduled_at < :to
            ORDER BY scheduled_at""", nativeQuery = true)
    List<Interview> findBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
            SELECT * FROM interview WHERE scheduled_at >= :from AND scheduled_at < :to
              AND :userId = ANY (interviewer_ids)
            ORDER BY scheduled_at""", nativeQuery = true)
    List<Interview> findBetweenForInterviewer(@Param("from") Instant from, @Param("to") Instant to,
            @Param("userId") String userId);

    @Query(value = """
            SELECT EXISTS (SELECT 1 FROM interview
                           WHERE application_id = :applicationId AND :userId = ANY (interviewer_ids))""",
            nativeQuery = true)
    boolean isInterviewer(@Param("applicationId") UUID applicationId, @Param("userId") String userId);
}
