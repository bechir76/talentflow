package com.talentflow.candidate.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.talentflow.candidate.domain.Candidate;

interface SpringDataCandidateRepository extends JpaRepository<Candidate, UUID> {

    Optional<Candidate> findByKeycloakUserId(String keycloakUserId);
}
