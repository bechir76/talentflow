package com.talentflow.candidate.domain;

import java.util.Optional;

/**
 * Persistence port for candidates and their consents.
 */
public interface CandidateRepository {

    Candidate save(Candidate candidate);

    Optional<Candidate> findByKeycloakUserId(String keycloakUserId);

    Consent save(Consent consent);
}
