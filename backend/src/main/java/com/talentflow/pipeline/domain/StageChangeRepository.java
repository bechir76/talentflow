package com.talentflow.pipeline.domain;

import java.util.List;
import java.util.UUID;

/**
 * Persistence port for the stage history.
 */
public interface StageChangeRepository {

    StageChange save(StageChange change);

    /** Newest first. */
    List<StageChange> findByApplicationId(UUID applicationId);
}
