package com.talentflow.audit.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.talentflow.audit.domain.AuditAction;
import com.talentflow.audit.domain.AuditLog;
import com.talentflow.audit.domain.AuditLogRepository;

@Repository
class JpaAuditLogRepository implements AuditLogRepository {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "timestamp");

    private final SpringDataAuditLogRepository jpa;

    JpaAuditLogRepository(SpringDataAuditLogRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public AuditLog save(AuditLog entry) {
        return jpa.save(entry);
    }

    @Override
    public Page<AuditLog> search(AuditAction action, Pageable pageable) {
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), NEWEST_FIRST);
        return action == null ? jpa.findAll(sorted) : jpa.findByAction(action, sorted);
    }
}
