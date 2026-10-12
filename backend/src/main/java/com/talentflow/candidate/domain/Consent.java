package com.talentflow.candidate.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Record of a consent given by a candidate, kept as proof (GDPR Art. 7(1)).
 */
@Entity
@Table(name = "consent")
public class Consent {

    @Id
    private UUID id;

    @Column(name = "candidate_id")
    private UUID candidateId;

    @Enumerated(EnumType.STRING)
    private ConsentType type;

    @Column(name = "policy_version")
    private String policyVersion;

    @Column(name = "given_at")
    private Instant givenAt;

    @Column(name = "withdrawn_at")
    private Instant withdrawnAt;

    protected Consent() {
    }

    public static Consent give(UUID candidateId, ConsentType type, String policyVersion, Instant now) {
        Consent consent = new Consent();
        consent.id = UUID.randomUUID();
        consent.candidateId = candidateId;
        consent.type = type;
        consent.policyVersion = policyVersion;
        consent.givenAt = now;
        return consent;
    }

    public void withdraw(Instant now) {
        if (withdrawnAt == null) {
            withdrawnAt = now;
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getCandidateId() {
        return candidateId;
    }

    public ConsentType getType() {
        return type;
    }

    public String getPolicyVersion() {
        return policyVersion;
    }

    public Instant getGivenAt() {
        return givenAt;
    }

    public Instant getWithdrawnAt() {
        return withdrawnAt;
    }
}
