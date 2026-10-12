package com.talentflow.gdpr.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Everything TalentFlow stores about a candidate, as handed out for a subject access request (Art. 15).
 * It also includes the AI screening results, because they are derived from the candidate's data.
 */
public record CandidateDataExport(
        Instant exportedAt,
        Profile profile,
        List<ConsentRecord> consents,
        List<ApplicationRecord> applications) {

    public record Profile(UUID id, String fullName, String email, String phone, String location, Instant createdAt) {
    }

    public record ConsentRecord(String type, String policyVersion, Instant givenAt, Instant withdrawnAt) {
    }

    public record ApplicationRecord(UUID id, String jobTitle, String stage, Instant appliedAt, String coverLetter,
            boolean cvStored, AiScreening aiScreening, List<StageRecord> stageHistory,
            List<InterviewRecord> interviews) {
    }

    public record AiScreening(String status, Integer score, List<String> matchingSkills, List<String> missingSkills,
            Integer yearsOfExperience, String summary, String model, Instant analyzedAt,
            String notice) {
    }

    public record StageRecord(String from, String to, Instant changedAt) {
    }

    public record InterviewRecord(Instant scheduledAt, int durationMinutes, String meetingLink) {
    }
}
