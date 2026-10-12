package com.talentflow.application.application;

import java.util.UUID;

/**
 * Port to the interview module: hiring managers may only see applications they interview.
 */
public interface InterviewAssignments {

    boolean isInterviewer(UUID applicationId, String userId);
}
