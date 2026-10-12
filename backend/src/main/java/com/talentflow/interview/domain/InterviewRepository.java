package com.talentflow.interview.domain;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for interviews and their feedback.
 */
public interface InterviewRepository {

    Interview save(Interview interview);

    Optional<Interview> findById(UUID id);

    void delete(Interview interview);

    /** Ordered by start time. */
    List<Interview> findByApplicationId(UUID applicationId);

    /** Interviews starting in [from, to), ordered by start time; {@code interviewerId} null means everyone's. */
    List<Interview> findBetween(Instant from, Instant to, String interviewerId);

    boolean isInterviewer(UUID applicationId, String userId);

    Feedback save(Feedback feedback);

    boolean hasFeedback(UUID interviewId, String authorId);

    List<Feedback> findFeedback(Collection<UUID> interviewIds);
}
