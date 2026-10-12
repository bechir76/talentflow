package com.talentflow.pipeline.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.talentflow.pipeline.domain.StageChange;

interface SpringDataStageChangeRepository extends JpaRepository<StageChange, UUID> {

    List<StageChange> findByApplicationIdOrderByChangedAtDesc(UUID applicationId);
}
