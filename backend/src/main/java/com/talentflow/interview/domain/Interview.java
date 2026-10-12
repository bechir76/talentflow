package com.talentflow.interview.domain;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.talentflow.shared.domain.BusinessRuleException;

/**
 * A scheduled interview for an application, with one or more interviewers.
 */
@Entity
@Table(name = "interview")
public class Interview {

    public static final int MIN_DURATION = 15;
    public static final int MAX_DURATION = 480;

    @Id
    private UUID id;

    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "duration_minutes")
    private int durationMinutes;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "interviewer_ids", columnDefinition = "text[]")
    private List<String> interviewerIds = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "interviewer_names", columnDefinition = "text[]")
    private List<String> interviewerNames = new ArrayList<>();

    @Column(name = "meeting_link")
    private String meetingLink;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private Instant createdAt;

    protected Interview() {
    }

    public static Interview schedule(UUID applicationId, Instant scheduledAt, int durationMinutes,
            List<Interviewer> interviewers, String meetingLink, String createdBy, Instant now) {
        if (!scheduledAt.isAfter(now)) {
            throw new BusinessRuleException("Interviews must be scheduled in the future.");
        }
        if (durationMinutes < MIN_DURATION || durationMinutes > MAX_DURATION) {
            throw new BusinessRuleException("An interview lasts between 15 minutes and 8 hours.");
        }
        if (interviewers.isEmpty()) {
            throw new BusinessRuleException("Invite at least one interviewer.");
        }
        Interview interview = new Interview();
        interview.id = UUID.randomUUID();
        interview.applicationId = applicationId;
        interview.scheduledAt = scheduledAt;
        interview.durationMinutes = durationMinutes;
        interviewers.stream().distinct().forEach(interviewer -> {
            interview.interviewerIds.add(interviewer.id());
            interview.interviewerNames.add(interviewer.name());
        });
        interview.meetingLink = validLink(meetingLink);
        interview.createdBy = createdBy;
        interview.createdAt = now;
        return interview;
    }

    public boolean hasInterviewer(String userId) {
        return interviewerIds.contains(userId);
    }

    /** Part of the candidate's erasure: the link may identify a personal meeting room. */
    public void anonymize() {
        meetingLink = null;
    }

    public List<Interviewer> getInterviewers() {
        List<Interviewer> interviewers = new ArrayList<>();
        for (int i = 0; i < interviewerIds.size(); i++) {
            interviewers.add(new Interviewer(interviewerIds.get(i), interviewerNames.get(i)));
        }
        return interviewers;
    }

    private static String validLink(String link) {
        if (link == null || link.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(link.strip());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
                throw new IllegalArgumentException();
            }
            return uri.toString();
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("The meeting link must be an https:// address.");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public record Interviewer(String id, String name) {
    }
}
