package com.talentflow.pipeline.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.talentflow.pipeline.domain.StageChange;
import com.talentflow.pipeline.domain.StageChangeRepository;

@Repository
class JpaStageChangeRepository implements StageChangeRepository {

    private final SpringDataStageChangeRepository jpa;

    JpaStageChangeRepository(SpringDataStageChangeRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public StageChange save(StageChange change) {
        return jpa.save(change);
    }

    @Override
    public List<StageChange> findByApplicationId(UUID applicationId) {
        return jpa.findByApplicationIdOrderByChangedAtDesc(applicationId);
    }
}
