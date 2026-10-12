package com.talentflow.pipeline.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.talentflow.application.domain.ApplicationStage;

/**
 * History entry: who moved an application from one stage to another, and when.
 */
@Entity
@Table(name = "stage_change")
public class StageChange {

    public static final int MAX_NOTE_LENGTH = 500;

    @Id
    private UUID id;

    @Column(name = "application_id")
    private UUID applicationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_stage")
    private ApplicationStage fromStage;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_stage")
    private ApplicationStage toStage;

    @Column(name = "changed_by")
    private String changedBy;

    /** Display name at the time of the change, so the history stays readable without a user directory. */
    @Column(name = "changed_by_name")
    private String changedByName;

    @Column(name = "changed_at")
    private Instant changedAt;

    private String note;

    protected StageChange() {
    }

    public static StageChange record(UUID applicationId, ApplicationStage from, ApplicationStage to,
            String changedBy, String changedByName, String note, Instant now) {
        StageChange change = new StageChange();
        change.id = UUID.randomUUID();
        change.applicationId = applicationId;
        change.fromStage = from;
        change.toStage = to;
        change.changedBy = changedBy;
        change.changedByName = changedByName;
        change.note = note == null || note.isBlank() ? null : note.strip();
        change.changedAt = now;
        return change;
    }

    public UUID getId() {
        return id;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public ApplicationStage getFromStage() {
        return fromStage;
    }

    public ApplicationStage getToStage() {
        return toStage;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public String getNote() {
        return note;
    }
}
