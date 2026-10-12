package com.talentflow.gdpr.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.talentflow.shared.domain.BusinessRuleException;

class RetentionPolicyTest {

    private static final Instant NOW = Instant.parse("2026-10-01T10:00:00Z");

    @Test
    void updatesPeriodAndSwitch() {
        RetentionPolicy policy = new RetentionPolicy(UUID.randomUUID(), 6, false, NOW);

        policy.update(24, true, NOW.plusSeconds(10));

        assertThat(policy.getRejectedRetentionMonths()).isEqualTo(24);
        assertThat(policy.isAutoDeleteEnabled()).isTrue();
        assertThat(policy.getUpdatedAt()).isEqualTo(NOW.plusSeconds(10));
    }

    @Test
    void rejectsPeriodsOutsideOneToOneHundredTwentyMonths() {
        RetentionPolicy policy = new RetentionPolicy(UUID.randomUUID(), 6, false, NOW);

        assertThatThrownBy(() -> policy.update(0, true, NOW)).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> policy.update(121, true, NOW)).isInstanceOf(BusinessRuleException.class);
    }
}
