package com.talentflow.audit.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.talentflow.audit.domain.AuditAction;
import com.talentflow.audit.domain.AuditLog;

interface SpringDataAuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);
}
