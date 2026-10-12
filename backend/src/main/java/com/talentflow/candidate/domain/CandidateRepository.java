package com.talentflow.candidate.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for candidates and their consents.
 */
public interface CandidateRepository {

    Candidate save(Candidate candidate);

    Optional<Candidate> findById(UUID id);

    Optional<Candidate> findByKeycloakUserId(String keycloakUserId);

    /** The most recent consent of the given type, if any. */
    Optional<Consent> findLatestConsent(UUID candidateId, ConsentType type);

    Consent save(Consent consent);
}
