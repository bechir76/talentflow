package com.talentflow.candidate.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.talentflow.candidate.domain.Candidate;
import com.talentflow.candidate.domain.CandidateRepository;
import com.talentflow.candidate.domain.Consent;
import com.talentflow.candidate.domain.ConsentType;

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
    public Optional<Candidate> findById(UUID id) {
        return candidates.findById(id);
    }

    @Override
    public List<Candidate> findAllById(Collection<UUID> ids) {
        return candidates.findAllById(ids);
    }

    @Override
    public Optional<Consent> findLatestConsent(UUID candidateId, ConsentType type) {
        return consents.findFirstByCandidateIdAndTypeOrderByGivenAtDesc(candidateId, type);
    }

    @Override
    public Optional<Candidate> findByKeycloakUserId(String keycloakUserId) {
        return candidates.findByKeycloakUserId(keycloakUserId);
    }

    @Override
    public List<Consent> findConsents(UUID candidateId) {
        return consents.findByCandidateIdOrderByGivenAtDesc(candidateId);
    }

    @Override
    public Consent save(Consent consent) {
        return consents.save(consent);
    }
}
