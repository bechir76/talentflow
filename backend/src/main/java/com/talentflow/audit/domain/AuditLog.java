package com.talentflow.audit.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * One entry in the append-only audit trail. Entries are never updated or deleted by the application.
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    private UUID id;

    @Column(name = "actor_id")
    private String actorId;

    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "occurred_at")
    private Instant timestamp;

    @Column(columnDefinition = "text")
    private String details;

    protected AuditLog() {
    }

    public static AuditLog of(String actorId, AuditAction action, String entityType, UUID entityId, String details,
            Instant now) {
        AuditLog entry = new AuditLog();
        entry.id = UUID.randomUUID();
        entry.actorId = actorId;
        entry.action = action;
        entry.entityType = entityType;
        entry.entityId = entityId;
        entry.details = details;
        entry.timestamp = now;
        return entry;
    }

    public UUID getId() {
        return id;
    }

    public String getActorId() {
        return actorId;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getDetails() {
        return details;
    }
}
