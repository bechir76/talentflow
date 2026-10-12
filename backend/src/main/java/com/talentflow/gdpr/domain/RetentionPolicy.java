package com.talentflow.gdpr.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.talentflow.shared.domain.BusinessRuleException;

/**
 * How long data of rejected candidates is kept before it is anonymised (GDPR storage limitation, Art. 5(1)(e)).
 */
@Entity
@Table(name = "retention_policy")
public class RetentionPolicy {

    public static final int MIN_MONTHS = 1;
    public static final int MAX_MONTHS = 120;

    @Id
    private UUID id;

    @Column(name = "rejected_retention_months")
    private int rejectedRetentionMonths;

    @Column(name = "auto_delete_enabled")
    private boolean autoDeleteEnabled;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected RetentionPolicy() {
    }

    public RetentionPolicy(UUID id, int rejectedRetentionMonths, boolean autoDeleteEnabled, Instant now) {
        this.id = id;
        update(rejectedRetentionMonths, autoDeleteEnabled, now);
    }

    public void update(int months, boolean enabled, Instant now) {
        if (months < MIN_MONTHS || months > MAX_MONTHS) {
            throw new BusinessRuleException("The retention period must be between 1 and 120 months.");
        }
        rejectedRetentionMonths = months;
        autoDeleteEnabled = enabled;
        updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public int getRejectedRetentionMonths() {
        return rejectedRetentionMonths;
    }

    public boolean isAutoDeleteEnabled() {
        return autoDeleteEnabled;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
