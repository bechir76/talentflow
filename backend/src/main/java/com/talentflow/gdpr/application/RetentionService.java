package com.talentflow.gdpr.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentflow.audit.application.AuditService;
import com.talentflow.audit.domain.AuditAction;
import com.talentflow.gdpr.domain.RetentionPolicy;
import com.talentflow.gdpr.domain.RetentionPolicyRepository;

/**
 * Applies the retention policy: rejected candidates are anonymised once their last rejection is older than the
 * configured number of months. Runs daily when enabled; admins can also start it manually.
 */
@Service
public class RetentionService {

    static final String JOB_NAME = "Retention job";
    private static final Logger log = LoggerFactory.getLogger(RetentionService.class);

    private final RetentionPolicyRepository policies;
    private final CandidateErasureService erasure;
    private final AuditService audit;
    private final Clock clock;

    public RetentionService(RetentionPolicyRepository policies, CandidateErasureService erasure, AuditService audit,
            Clock clock) {
        this.policies = policies;
        this.erasure = erasure;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public RetentionPolicy policy() {
        return policies.get();
    }

    @Transactional
    public RetentionPolicy updatePolicy(int months, boolean autoDeleteEnabled) {
        RetentionPolicy policy = policies.get();
        policy.update(months, autoDeleteEnabled, clock.instant());
        audit.record(AuditAction.RETENTION_POLICY_CHANGED, "RetentionPolicy", policy.getId(),
                months + " months, automatic deletion " + (autoDeleteEnabled ? "on" : "off"));
        return policy;
    }

    @Scheduled(cron = "${talentflow.retention.cron}")
    public void scheduledRun() {
        if (policies.get().isAutoDeleteEnabled()) {
            int count = anonymizeExpired();
            log.info("Retention job anonymised {} candidate(s)", count);
        }
    }

    /** Anonymises all candidates whose retention period has expired, regardless of the automatic switch. */
    @Transactional
    public int anonymizeExpired() {
        RetentionPolicy policy = policies.get();
        Instant now = clock.instant();
        Instant cutoff = now.atOffset(ZoneOffset.UTC).minusMonths(policy.getRejectedRetentionMonths()).toInstant();
        List<UUID> expired = policies.findExpiredRejectedCandidates(cutoff);
        for (UUID candidateId : expired) {
            erasure.erase(candidateId, now);
            audit.recordAsSystem(JOB_NAME, AuditAction.CANDIDATE_DELETED, "Candidate", candidateId,
                    "Retention period of " + policy.getRejectedRetentionMonths() + " months expired");
        }
        return expired.size();
    }
}
