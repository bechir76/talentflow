package com.talentflow.candidate.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A person applying for jobs. Linked to a Keycloak account when the candidate signed up themselves.
 */
@Entity
@Table(name = "candidate")
public class Candidate {

    @Id
    private UUID id;

    @Column(name = "keycloak_user_id")
    private String keycloakUserId;

    @Column(name = "full_name")
    private String fullName;

    private String email;

    private String phone;

    private String location;

    private boolean anonymized;

    @Column(name = "created_at")
    private Instant createdAt;

    protected Candidate() {
    }

    public static Candidate register(String keycloakUserId, String fullName, String email, Instant now) {
        Candidate candidate = new Candidate();
        candidate.id = UUID.randomUUID();
        candidate.keycloakUserId = keycloakUserId;
        candidate.fullName = fullName;
        candidate.email = email;
        candidate.createdAt = now;
        return candidate;
    }

    /** Keeps the profile in sync with the identity provider and the latest application form. */
    public void updateContactDetails(String fullName, String email, String phone, String location) {
        this.fullName = fullName;
        this.email = email;
        if (phone != null) {
            this.phone = phone;
        }
        if (location != null) {
            this.location = location;
        }
    }

    public UUID getId() {
        return id;
    }

    public String getKeycloakUserId() {
        return keycloakUserId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getLocation() {
        return location;
    }

    public boolean isAnonymized() {
        return anonymized;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
