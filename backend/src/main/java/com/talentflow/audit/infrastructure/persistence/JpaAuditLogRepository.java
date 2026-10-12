package com.talentflow.audit.infrastructure.persistence;

import org.springframework.stereotype.Repository;

import com.talentflow.audit.domain.AuditLog;
import com.talentflow.audit.domain.AuditLogRepository;

@Repository
class JpaAuditLogRepository implements AuditLogRepository {

    private final SpringDataAuditLogRepository jpa;

    JpaAuditLogRepository(SpringDataAuditLogRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public AuditLog save(AuditLog entry) {
        return jpa.save(entry);
    }
}
