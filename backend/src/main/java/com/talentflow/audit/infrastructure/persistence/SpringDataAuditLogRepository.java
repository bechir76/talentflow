package com.talentflow.audit.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.talentflow.audit.domain.AuditLog;

interface SpringDataAuditLogRepository extends JpaRepository<AuditLog, UUID> {
}
