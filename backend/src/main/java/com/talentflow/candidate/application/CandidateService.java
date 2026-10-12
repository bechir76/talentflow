package com.talentflow.candidate.application;

import java.time.Clock;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentflow.candidate.domain.Candidate;
import com.talentflow.candidate.domain.CandidateRepository;
import com.talentflow.candidate.domain.Consent;
import com.talentflow.candidate.domain.ConsentType;
import com.talentflow.shared.application.CurrentUser;
import com.talentflow.shared.domain.BusinessRuleException;

@Service
@Transactional
public class CandidateService {

    private final CandidateRepository candidates;
    private final Clock clock;
    private final String privacyPolicyVersion;

    public CandidateService(CandidateRepository candidates, Clock clock,
            @Value("${talentflow.privacy.policy-version}") String privacyPolicyVersion) {
        this.candidates = candidates;
        this.clock = clock;
        this.privacyPolicyVersion = privacyPolicyVersion;
    }

    /**
     * Returns the candidate profile of a signed-in user, creating it on their first application.
     * Name and email always come from the identity provider.
     */
    public Candidate registerApplicant(CurrentUser user, String phone, String location) {
        if (user.email() == null || user.fullName() == null) {
            throw new BusinessRuleException("Your account needs a name and an email address to apply.");
        }
        Candidate candidate = candidates.findByKeycloakUserId(user.id())
                .orElseGet(() -> Candidate.register(user.id(), user.fullName(), user.email(), clock.instant()));
        candidate.updateContactDetails(user.fullName(), user.email(), phone, location);
        return candidates.save(candidate);
    }

    /** Stores proof of consent against the current privacy policy version. */
    public Consent recordConsent(Candidate candidate, ConsentType type) {
        return candidates.save(Consent.give(candidate.getId(), type, privacyPolicyVersion, clock.instant()));
    }

    @Transactional(readOnly = true)
    public Optional<Candidate> findByUser(CurrentUser user) {
        return candidates.findByKeycloakUserId(user.id());
    }
}
