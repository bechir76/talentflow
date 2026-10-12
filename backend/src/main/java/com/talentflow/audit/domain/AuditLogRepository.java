package com.talentflow.audit.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Persistence port for the append-only audit trail.
 */
public interface AuditLogRepository {

    AuditLog save(AuditLog entry);

    /** Newest first; {@code action} null means all actions. */
    Page<AuditLog> search(AuditAction action, Pageable pageable);
}
