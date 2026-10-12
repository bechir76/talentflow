package com.talentflow.job.application;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Port to the applications module, so the job module can show applicant numbers without depending on it.
 */
public interface ApplicantCounter {

    /** Number of applications per job; jobs without applications may be missing from the map. */
    Map<UUID, Long> countByJobIds(Collection<UUID> jobIds);

    /** Applications that are neither hired nor rejected. */
    long countActive();
}
