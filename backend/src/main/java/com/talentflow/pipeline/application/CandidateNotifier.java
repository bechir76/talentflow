package com.talentflow.pipeline.application;

import com.talentflow.application.domain.ApplicationStage;

/**
 * Port for telling candidates about progress on their application.
 */
public interface CandidateNotifier {

    void stageChanged(String email, String fullName, String jobTitle, ApplicationStage newStage);
}
