package com.talentflow.interview.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.talentflow.interview.domain.Feedback;

interface SpringDataFeedbackRepository extends JpaRepository<Feedback, UUID> {

    boolean existsByInterviewIdAndAuthorId(UUID interviewId, String authorId);

    List<Feedback> findByInterviewIdInOrderBySubmittedAt(Collection<UUID> interviewIds);
}
