package com.talentflow.interview.application;

import java.util.UUID;

/**
 * Published after an interview was scheduled; the candidate gets an invitation once the transaction commits.
 */
public record InterviewScheduled(UUID interviewId) {
}
