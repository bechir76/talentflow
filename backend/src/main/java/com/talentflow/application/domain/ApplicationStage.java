package com.talentflow.application.domain;

/**
 * Pipeline stage of an application: APPLIED → SCREENING → INTERVIEW → OFFER → HIRED, or REJECTED from any stage.
 */
public enum ApplicationStage {
    APPLIED,
    SCREENING,
    INTERVIEW,
    OFFER,
    HIRED,
    REJECTED;

    /** Hired and rejected applications are finished; all others are still in progress. */
    public boolean isActive() {
        return this != HIRED && this != REJECTED;
    }
}
