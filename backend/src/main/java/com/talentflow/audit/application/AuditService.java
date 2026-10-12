package com.talentflow.audit.application;

import java.time.Clock;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.talentflow.audit.domain.AuditAction;
import com.talentflow.audit.domain.AuditLog;
import com.talentflow.audit.domain.AuditLogRepository;
import com.talentflow.shared.application.CurrentUserProvider;

/**
 * Records sensitive actions. Joins the caller's transaction, so an entry exists exactly when the action
 * was committed.
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLog;
    private final CurrentUserProvider currentUser;
    private final Clock clock;

    public AuditService(AuditLogRepository auditLog, CurrentUserProvider currentUser, Clock clock) {
        this.auditLog = auditLog;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(AuditAction action, String entityType, UUID entityId, String details) {
        auditLog.save(AuditLog.of(currentUser.currentUser().id(), action, entityType, entityId, details,
                clock.instant()));
    }
}
