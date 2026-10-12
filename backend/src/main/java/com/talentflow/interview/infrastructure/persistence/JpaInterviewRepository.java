package com.talentflow.interview.infrastructure.persistence;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.talentflow.application.application.InterviewAssignments;
import com.talentflow.interview.domain.Feedback;
import com.talentflow.interview.domain.Interview;
import com.talentflow.interview.domain.InterviewRepository;

/**
 * JPA adapter for interviews. Also tells the applications module who is an interviewer of an application.
 */
@Repository
class JpaInterviewRepository implements InterviewRepository, InterviewAssignments {

    private final SpringDataInterviewRepository interviews;
    private final SpringDataFeedbackRepository feedback;

    JpaInterviewRepository(SpringDataInterviewRepository interviews, SpringDataFeedbackRepository feedback) {
        this.interviews = interviews;
        this.feedback = feedback;
    }

    @Override
    public Interview save(Interview interview) {
        return interviews.save(interview);
    }

    @Override
    public Optional<Interview> findById(UUID id) {
        return interviews.findById(id);
    }

    @Override
    public void delete(Interview interview) {
        interviews.delete(interview);
    }

    @Override
    public List<Interview> findByApplicationId(UUID applicationId) {
        return interviews.findByApplicationIdOrderByScheduledAt(applicationId);
    }

    @Override
    public List<Interview> findBetween(Instant from, Instant to, String interviewerId) {
        return interviewerId == null ? interviews.findBetween(from, to)
                : interviews.findBetweenForInterviewer(from, to, interviewerId);
    }

    @Override
    public boolean isInterviewer(UUID applicationId, String userId) {
        return interviews.isInterviewer(applicationId, userId);
    }

    @Override
    public Feedback save(Feedback entry) {
        return feedback.save(entry);
    }

    @Override
    public boolean hasFeedback(UUID interviewId, String authorId) {
        return feedback.existsByInterviewIdAndAuthorId(interviewId, authorId);
    }

    @Override
    public List<Feedback> findFeedback(Collection<UUID> interviewIds) {
        return interviewIds.isEmpty() ? List.of() : feedback.findByInterviewIdInOrderBySubmittedAt(interviewIds);
    }
}
