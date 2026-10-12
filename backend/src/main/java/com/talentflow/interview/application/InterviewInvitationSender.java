package com.talentflow.interview.application;

import java.time.Instant;

/**
 * Port for telling a candidate about a scheduled interview.
 */
public interface InterviewInvitationSender {

    void send(String email, String fullName, String jobTitle, Instant scheduledAt, int durationMinutes,
            String meetingLink);
}
