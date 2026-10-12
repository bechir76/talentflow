package com.talentflow.candidate.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.talentflow.candidate.domain.Consent;
import com.talentflow.candidate.domain.ConsentType;

interface SpringDataConsentRepository extends JpaRepository<Consent, UUID> {

    List<Consent> findByCandidateIdOrderByGivenAtDesc(UUID candidateId);

    Optional<Consent> findFirstByCandidateIdAndTypeOrderByGivenAtDesc(UUID candidateId, ConsentType type);
}
