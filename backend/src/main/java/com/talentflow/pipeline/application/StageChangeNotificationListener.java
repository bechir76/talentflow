package com.talentflow.pipeline.application;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.talentflow.application.domain.JobApplicationRepository;
import com.talentflow.candidate.application.CandidateService;
import com.talentflow.candidate.domain.Candidate;
import com.talentflow.job.application.JobService;

/**
 * Emails the candidate after a committed stage change. A mail problem is logged and never undoes the change.
 */
@Component
public class StageChangeNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(StageChangeNotificationListener.class);

    private final JobApplicationRepository applications;
    private final CandidateService candidates;
    private final JobService jobs;
    private final CandidateNotifier notifier;

    public StageChangeNotificationListener(JobApplicationRepository applications, CandidateService candidates,
            JobService jobs, CandidateNotifier notifier) {
        this.applications = applications;
        this.candidates = candidates;
        this.jobs = jobs;
        this.notifier = notifier;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStageChanged(StageChanged event) {
        try {
            applications.findById(event.applicationId()).ifPresent(application -> {
                Candidate candidate = candidates.get(application.getCandidateId());
                if (candidate.isAnonymized()) {
                    return;
                }
                String jobTitle = jobs.findViews(List.of(application.getJobId())).get(application.getJobId()).job().getTitle();
                notifier.stageChanged(candidate.getEmail(), candidate.getFullName(), jobTitle, event.to());
            });
        } catch (RuntimeException e) {
            log.warn("Could not notify candidate about application {}: {}", event.applicationId(), e.getMessage());
        }
    }
}
