package com.talentflow.pipeline.application;

import java.util.UUID;

import com.talentflow.application.domain.ApplicationStage;

/**
 * Published when an application moved to another stage. Handled after commit (candidate email).
 */
public record StageChanged(UUID applicationId, ApplicationStage from, ApplicationStage to) {
}
