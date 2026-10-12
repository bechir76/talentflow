package com.talentflow.audit.domain;

/**
 * Persistence port for the audit trail (write-only for now).
 */
public interface AuditLogRepository {

    AuditLog save(AuditLog entry);
}
