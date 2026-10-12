package com.talentflow.application.application;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.talentflow.shared.application.CurrentUser;
import com.talentflow.shared.application.CurrentUserProvider;
import com.talentflow.shared.domain.ForbiddenException;
import com.talentflow.shared.domain.Role;

/**
 * Recruiters and admins see every application; hiring managers only those they are an interviewer for
 * ("view assigned candidates").
 */
@Component
public class ApplicationAccessGuard {

    private final CurrentUserProvider currentUser;
    private final InterviewAssignments assignments;

    public ApplicationAccessGuard(CurrentUserProvider currentUser, InterviewAssignments assignments) {
        this.currentUser = currentUser;
        this.assignments = assignments;
    }

    public void requireAccess(UUID applicationId) {
        CurrentUser user = currentUser.currentUser();
        if (user.hasRole(Role.RECRUITER) || user.hasRole(Role.ADMIN)) {
            return;
        }
        if (!user.hasRole(Role.HIRING_MANAGER) || !assignments.isInterviewer(applicationId, user.id())) {
            throw new ForbiddenException("You can only see candidates you interview.");
        }
    }
}
