package com.talentflow.interview.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.talentflow.shared.domain.BusinessRuleException;

/**
 * An interviewer's scorecard: three ratings from 1 to 5 and a recommendation.
 */
@Entity
@Table(name = "feedback")
public class Feedback {

    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;
    public static final int MAX_COMMENT_LENGTH = 4000;

    @Id
    private UUID id;

    @Column(name = "interview_id")
    private UUID interviewId;

    @Column(name = "author_id")
    private String authorId;

    @Column(name = "author_name")
    private String authorName;

    private int technical;

    private int communication;

    @Column(name = "culture_fit")
    private int cultureFit;

    @Column(columnDefinition = "text")
    private String comment;

    @Enumerated(EnumType.STRING)
    private Recommendation recommendation;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    protected Feedback() {
    }

    public static Feedback submit(UUID interviewId, String authorId, String authorName, Scores scores,
            String comment, Recommendation recommendation, Instant now) {
        String text = comment == null || comment.isBlank() ? null : comment.strip();
        if (text != null && text.length() > MAX_COMMENT_LENGTH) {
            throw new BusinessRuleException("The comment must not be longer than 4000 characters.");
        }
        Feedback feedback = new Feedback();
        feedback.id = UUID.randomUUID();
        feedback.interviewId = interviewId;
        feedback.authorId = authorId;
        feedback.authorName = authorName;
        feedback.technical = scores.technical();
        feedback.communication = scores.communication();
        feedback.cultureFit = scores.cultureFit();
        feedback.comment = text;
        feedback.recommendation = recommendation;
        feedback.submittedAt = now;
        return feedback;
    }

    /** Part of the candidate's erasure: free text about the candidate is removed, ratings stay anonymous. */
    public void anonymize() {
        comment = null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getInterviewId() {
        return interviewId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public int getTechnical() {
        return technical;
    }

    public int getCommunication() {
        return communication;
    }

    public int getCultureFit() {
        return cultureFit;
    }

    public String getComment() {
        return comment;
    }

    public Recommendation getRecommendation() {
        return recommendation;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    /** The three ratings of a scorecard, each from 1 (poor) to 5 (excellent). */
    public record Scores(int technical, int communication, int cultureFit) {

        public Scores {
            for (int score : new int[] {technical, communication, cultureFit}) {
                if (score < MIN_RATING || score > MAX_RATING) {
                    throw new BusinessRuleException("Ratings must be between 1 and 5.");
                }
            }
        }

        public double average() {
            return (technical + communication + cultureFit) / 3.0;
        }
    }
}
