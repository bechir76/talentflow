package com.talentflow.gdpr.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.talentflow.gdpr.domain.RetentionPolicy;

interface SpringDataRetentionPolicyRepository extends JpaRepository<RetentionPolicy, UUID> {
}
