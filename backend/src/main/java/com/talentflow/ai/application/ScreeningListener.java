package com.talentflow.ai.application;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.talentflow.application.application.ScreeningRequested;

/**
 * Starts the screening in the background once the application is committed, so applying stays fast
 * and a screening problem can never roll back an application.
 */
@Component
public class ScreeningListener {

    private final ScreeningService screening;

    public ScreeningListener(ScreeningService screening) {
        this.screening = screening;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onScreeningRequested(ScreeningRequested event) {
        screening.screen(event.applicationId());
    }
}
