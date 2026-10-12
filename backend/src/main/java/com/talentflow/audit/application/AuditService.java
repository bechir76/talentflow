package com.talentflow.audit.application;

import java.time.Clock;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.talentflow.audit.domain.AuditAction;
import com.talentflow.audit.domain.AuditLog;
import com.talentflow.audit.domain.AuditLogRepository;
import com.talentflow.shared.application.CurrentUser;
import com.talentflow.shared.application.CurrentUserProvider;

/**
 * Records sensitive actions. Writes join the caller's transaction, so an entry exists exactly when the action
 * was committed.
 */
@Service
public class AuditService {

    public static final String SYSTEM_ACTOR = "system";

    private final AuditLogRepository auditLog;
    private final CurrentUserProvider currentUser;
    private final Clock clock;

    public AuditService(AuditLogRepository auditLog, CurrentUserProvider currentUser, Clock clock) {
        this.auditLog = auditLog;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    /** Records an action of the signed-in user. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(AuditAction action, String entityType, UUID entityId, String details) {
        CurrentUser user = currentUser.currentUser();
        auditLog.save(AuditLog.of(user.id(), user.fullName(), action, entityType, entityId, details, clock.instant()));
    }

    /** Records an action of a background job (no signed-in user). */
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordAsSystem(String jobName, AuditAction action, String entityType, UUID entityId, String details) {
        auditLog.save(AuditLog.of(SYSTEM_ACTOR, jobName, action, entityType, entityId, details, clock.instant()));
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> search(AuditAction action, Pageable pageable) {
        return auditLog.search(action, pageable);
    }
}
