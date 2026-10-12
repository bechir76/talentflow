package com.talentflow.interview.application;

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
import com.talentflow.interview.domain.InterviewRepository;
import com.talentflow.job.application.JobService;

/**
 * Emails the candidate after an interview was scheduled. Failures are logged, never rolled back.
 */
@Component
public class InterviewInvitationListener {

    private static final Logger log = LoggerFactory.getLogger(InterviewInvitationListener.class);

    private final InterviewRepository interviews;
    private final JobApplicationRepository applications;
    private final CandidateService candidates;
    private final JobService jobs;
    private final InterviewInvitationSender sender;

    public InterviewInvitationListener(InterviewRepository interviews, JobApplicationRepository applications,
            CandidateService candidates, JobService jobs, InterviewInvitationSender sender) {
        this.interviews = interviews;
        this.applications = applications;
        this.candidates = candidates;
        this.jobs = jobs;
        this.sender = sender;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInterviewScheduled(InterviewScheduled event) {
        try {
            interviews.findById(event.interviewId()).ifPresent(interview ->
                    applications.findById(interview.getApplicationId()).ifPresent(application -> {
                        Candidate candidate = candidates.get(application.getCandidateId());
                        if (candidate.isAnonymized()) {
                            return;
                        }
                        String jobTitle = jobs.findViews(List.of(application.getJobId()))
                                .get(application.getJobId()).job().getTitle();
                        sender.send(candidate.getEmail(), candidate.getFullName(), jobTitle,
                                interview.getScheduledAt(), interview.getDurationMinutes(), interview.getMeetingLink());
                    }));
        } catch (RuntimeException e) {
            log.warn("Could not send interview invitation for {}: {}", event.interviewId(), e.getMessage());
        }
    }
}
