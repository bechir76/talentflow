package com.talentflow.candidate.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for candidates and their consents.
 */
public interface CandidateRepository {

    Candidate save(Candidate candidate);

    Optional<Candidate> findById(UUID id);

    List<Candidate> findAllById(Collection<UUID> ids);

    Optional<Candidate> findByKeycloakUserId(String keycloakUserId);

    /** The most recent consent of the given type, if any. */
    Optional<Consent> findLatestConsent(UUID candidateId, ConsentType type);

    Consent save(Consent consent);
}
