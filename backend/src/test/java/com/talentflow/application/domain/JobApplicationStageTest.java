package com.talentflow.application.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.talentflow.shared.domain.BusinessRuleException;

class JobApplicationStageTest {

    private static final Instant APPLIED_AT = Instant.parse("2026-10-01T10:00:00Z");
    private static final Instant LATER = APPLIED_AT.plusSeconds(3600);

    private final JobApplication application =
            JobApplication.submit(UUID.randomUUID(), UUID.randomUUID(), "cv.pdf", null, APPLIED_AT);

    @Test
    void startsInAppliedSinceApplying() {
        assertThat(application.getStage()).isEqualTo(ApplicationStage.APPLIED);
        assertThat(application.getStageChangedAt()).isEqualTo(APPLIED_AT);
    }

    @Test
    void movesForwardAndBackAmongActiveStages() {
        assertThat(application.moveTo(ApplicationStage.INTERVIEW, LATER)).isEqualTo(ApplicationStage.APPLIED);
        assertThat(application.moveTo(ApplicationStage.SCREENING, LATER)).isEqualTo(ApplicationStage.INTERVIEW);
        assertThat(application.getStageChangedAt()).isEqualTo(LATER);
    }

    @Test
    void hiresOnlyFromOffer() {
        assertThatThrownBy(() -> application.moveTo(ApplicationStage.HIRED, LATER))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("offer");

        application.moveTo(ApplicationStage.OFFER, LATER);
        application.moveTo(ApplicationStage.HIRED, LATER);
        assertThat(application.getStage()).isEqualTo(ApplicationStage.HIRED);
    }

    @Test
    void rejectsFromAnyActiveStageAndThenStays() {
        application.moveTo(ApplicationStage.SCREENING, LATER);
        application.moveTo(ApplicationStage.REJECTED, LATER);

        assertThatThrownBy(() -> application.moveTo(ApplicationStage.APPLIED, LATER))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("cannot be moved");
    }

    @Test
    void rejectsMoveToTheCurrentStage() {
        assertThatThrownBy(() -> application.moveTo(ApplicationStage.APPLIED, LATER))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("already");
    }
}
