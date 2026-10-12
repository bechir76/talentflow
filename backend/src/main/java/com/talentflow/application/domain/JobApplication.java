package com.talentflow.application.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.talentflow.shared.domain.BusinessRuleException;

/**
 * A candidate's application for a job, including the AI screening suggestion (never a decision).
 */
@Entity
@Table(name = "job_application")
public class JobApplication {

    public static final int MAX_COVER_LETTER_LENGTH = 5000;

    @Id
    private UUID id;

    @Version
    private long version;

    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "candidate_id")
    private UUID candidateId;

    @Column(name = "cv_storage_key")
    private String cvStorageKey;

    @Column(name = "cover_letter", columnDefinition = "text")
    private String coverLetter;

    @Enumerated(EnumType.STRING)
    private ApplicationStage stage;

    @Column(name = "ai_score")
    private Integer aiScore;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "ai_matching_skills", columnDefinition = "text[]")
    private List<String> aiMatchingSkills = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "ai_missing_skills", columnDefinition = "text[]")
    private List<String> aiMissingSkills = new ArrayList<>();

    @Column(name = "ai_summary", columnDefinition = "text")
    private String aiSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_status")
    private AiStatus aiStatus;

    @Column(name = "applied_at")
    private Instant appliedAt;

    protected JobApplication() {
    }

    public static JobApplication submit(UUID jobId, UUID candidateId, String cvStorageKey, String coverLetter,
            Instant now) {
        String letter = coverLetter == null || coverLetter.isBlank() ? null : coverLetter.strip();
        if (letter != null && letter.length() > MAX_COVER_LETTER_LENGTH) {
            throw new BusinessRuleException("The cover letter must not be longer than 5000 characters.");
        }
        JobApplication application = new JobApplication();
        application.id = UUID.randomUUID();
        application.jobId = jobId;
        application.candidateId = candidateId;
        application.cvStorageKey = cvStorageKey;
        application.coverLetter = letter;
        application.stage = ApplicationStage.APPLIED;
        application.aiStatus = AiStatus.PENDING;
        application.appliedAt = now;
        return application;
    }

    public UUID getId() {
        return id;
    }

    public UUID getJobId() {
        return jobId;
    }

    public UUID getCandidateId() {
        return candidateId;
    }

    public String getCvStorageKey() {
        return cvStorageKey;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public ApplicationStage getStage() {
        return stage;
    }

    public Integer getAiScore() {
        return aiScore;
    }

    public List<String> getAiMatchingSkills() {
        return List.copyOf(aiMatchingSkills);
    }

    public List<String> getAiMissingSkills() {
        return List.copyOf(aiMissingSkills);
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public AiStatus getAiStatus() {
        return aiStatus;
    }

    public Instant getAppliedAt() {
        return appliedAt;
    }
}
