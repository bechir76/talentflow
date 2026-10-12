package com.talentflow.candidate.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.talentflow.candidate.domain.Candidate;
import com.talentflow.candidate.domain.CandidateRepository;
import com.talentflow.candidate.domain.Consent;

@Repository
class JpaCandidateRepository implements CandidateRepository {

    private final SpringDataCandidateRepository candidates;
    private final SpringDataConsentRepository consents;

    JpaCandidateRepository(SpringDataCandidateRepository candidates, SpringDataConsentRepository consents) {
        this.candidates = candidates;
        this.consents = consents;
    }

    @Override
    public Candidate save(Candidate candidate) {
        return candidates.save(candidate);
    }

    @Override
    public Optional<Candidate> findByKeycloakUserId(String keycloakUserId) {
        return candidates.findByKeycloakUserId(keycloakUserId);
    }

    @Override
    public Consent save(Consent consent) {
        return consents.save(consent);
    }
}
