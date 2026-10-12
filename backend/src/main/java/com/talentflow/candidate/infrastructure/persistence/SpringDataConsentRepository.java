package com.talentflow.candidate.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.talentflow.candidate.domain.Consent;

interface SpringDataConsentRepository extends JpaRepository<Consent, UUID> {
}
