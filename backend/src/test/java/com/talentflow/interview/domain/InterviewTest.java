package com.talentflow.interview.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.talentflow.interview.domain.Feedback.Scores;
import com.talentflow.interview.domain.Interview.Interviewer;
import com.talentflow.shared.domain.BusinessRuleException;

class InterviewTest {

    private static final Instant NOW = Instant.parse("2026-10-01T10:00:00Z");
    private static final Instant TOMORROW = NOW.plusSeconds(86_400);
    private static final List<Interviewer> MARTIN = List.of(new Interviewer("kc-m", "Martin Richter"));

    @Test
    void schedulesWithInterviewersInOrder() {
        Interview interview = Interview.schedule(UUID.randomUUID(), TOMORROW, 60,
                List.of(new Interviewer("kc-m", "Martin"), new Interviewer("kc-a", "Anna"), new Interviewer("kc-m", "Martin")),
                " https://meet.example.org/abc ", "kc-a", NOW);

        assertThat(interview.getInterviewers()).extracting(Interviewer::id).containsExactly("kc-m", "kc-a");
        assertThat(interview.hasInterviewer("kc-a")).isTrue();
        assertThat(interview.getMeetingLink()).isEqualTo("https://meet.example.org/abc");
    }

    @Test
    void rejectsInvalidSchedules() {
        UUID applicationId = UUID.randomUUID();
        assertThatThrownBy(() -> Interview.schedule(applicationId, NOW.minusSeconds(60), 60, MARTIN, null, "kc", NOW))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("future");
        assertThatThrownBy(() -> Interview.schedule(applicationId, TOMORROW, 10, MARTIN, null, "kc", NOW))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> Interview.schedule(applicationId, TOMORROW, 60, List.of(), null, "kc", NOW))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("interviewer");
        assertThatThrownBy(() -> Interview.schedule(applicationId, TOMORROW, 60, MARTIN, "javascript:alert(1)", "kc", NOW))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("https");
    }

    @Test
    void anonymiseRemovesMeetingLinkAndComment() {
        Interview interview = Interview.schedule(UUID.randomUUID(), TOMORROW, 45, MARTIN, "https://meet.example.org/x", "kc", NOW);
        Feedback feedback = Feedback.submit(interview.getId(), "kc-m", "Martin", new Scores(4, 5, 3), "Strong", Recommendation.HIRE, NOW);

        interview.anonymize();
        feedback.anonymize();

        assertThat(interview.getMeetingLink()).isNull();
        assertThat(feedback.getComment()).isNull();
        assertThat(feedback.getTechnical()).isEqualTo(4);
    }

    @Test
    void scoresMustBeBetweenOneAndFive() {
        assertThatThrownBy(() -> new Scores(0, 3, 3)).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> new Scores(3, 6, 3)).isInstanceOf(BusinessRuleException.class);
        assertThat(new Scores(3, 4, 5).average()).isEqualTo(4.0);
    }
}
