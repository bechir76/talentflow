package com.talentflow.gdpr.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Persistence port for the retention policy and the candidates it applies to.
 */
public interface RetentionPolicyRepository {

    /** The single policy row created by the migration. */
    RetentionPolicy get();

    /**
     * Candidates that are not anonymised yet, have at least one application, and whose applications were all
     * rejected before {@code rejectedBefore}.
     */
    List<UUID> findExpiredRejectedCandidates(Instant rejectedBefore);
}
