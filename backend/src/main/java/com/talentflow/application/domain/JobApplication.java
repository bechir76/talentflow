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
import org.hibernate.annotations.OptimisticLock;
import org.hibernate.type.SqlTypes;

import com.talentflow.shared.domain.BusinessRuleException;

/**
 * A candidate's application for a job, including the AI screening suggestion (never a decision).
 * The AI fields are written by a background job and are excluded from optimistic locking, so a screening
 * finishing at the same moment never makes a recruiter's stage change fail.
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
    @OptimisticLock(excluded = true)
    private Integer aiScore;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "ai_matching_skills", columnDefinition = "text[]")
    @OptimisticLock(excluded = true)
    private List<String> aiMatchingSkills = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "ai_missing_skills", columnDefinition = "text[]")
    @OptimisticLock(excluded = true)
    private List<String> aiMissingSkills = new ArrayList<>();

    @Column(name = "ai_summary", columnDefinition = "text")
    @OptimisticLock(excluded = true)
    private String aiSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_status")
    @OptimisticLock(excluded = true)
    private AiStatus aiStatus;

    @Column(name = "ai_years_of_experience")
    @OptimisticLock(excluded = true)
    private Integer aiYearsOfExperience;

    @Column(name = "ai_model")
    @OptimisticLock(excluded = true)
    private String aiModel;

    @Column(name = "ai_analyzed_at")
    @OptimisticLock(excluded = true)
    private Instant aiAnalyzedAt;

    @Column(name = "applied_at")
    private Instant appliedAt;

    @Column(name = "stage_changed_at")
    private Instant stageChangedAt;

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
        application.stageChangedAt = now;
        return application;
    }

    /**
     * Moves the application through the pipeline. Active stages can move freely in both directions,
     * HIRED is only reachable from OFFER, REJECTED from any active stage, and finished applications stay put.
     *
     * @return the stage before the move
     */
    public ApplicationStage moveTo(ApplicationStage target, Instant now) {
        if (target == stage) {
            throw new BusinessRuleException("The application is already in stage " + stage + ".");
        }
        if (!stage.isActive()) {
            throw new BusinessRuleException("A " + stage + " application cannot be moved.");
        }
        if (target == ApplicationStage.HIRED && stage != ApplicationStage.OFFER) {
            throw new BusinessRuleException("Only candidates with an offer can be marked as hired.");
        }
        ApplicationStage previous = stage;
        stage = target;
        stageChangedAt = now;
        return previous;
    }

    /**
     * Removes the personal content of the application (cover letter, CV reference and the AI output derived
     * from the CV). Stage and dates stay for anonymous statistics. The caller deletes the CV file itself.
     */
    public void anonymize() {
        cvStorageKey = null;
        coverLetter = null;
        aiSummary = null;
        aiMatchingSkills = new ArrayList<>();
        aiMissingSkills = new ArrayList<>();
        aiYearsOfExperience = null;
    }

    /** Queues a new AI screening, for example after a failure. Earlier results stay visible until replaced. */
    public void requestAiAnalysis() {
        aiStatus = AiStatus.PENDING;
    }

    /** Stores the AI suggestion. It supports the recruiter; it never changes the stage. */
    public void recordAiAnalysis(int score, List<String> matchingSkills, List<String> missingSkills,
            Integer yearsOfExperience, String summary, String model, Instant now) {
        aiScore = score;
        aiMatchingSkills = new ArrayList<>(matchingSkills);
        aiMissingSkills = new ArrayList<>(missingSkills);
        aiYearsOfExperience = yearsOfExperience;
        aiSummary = summary;
        aiModel = model;
        aiAnalyzedAt = now;
        aiStatus = AiStatus.DONE;
    }

    public void markAiAnalysisFailed(Instant now) {
        aiStatus = AiStatus.FAILED;
        aiAnalyzedAt = now;
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

    public Integer getAiYearsOfExperience() {
        return aiYearsOfExperience;
    }

    public String getAiModel() {
        return aiModel;
    }

    public Instant getAiAnalyzedAt() {
        return aiAnalyzedAt;
    }

    public Instant getStageChangedAt() {
        return stageChangedAt;
    }

    public Instant getAppliedAt() {
        return appliedAt;
    }
}
