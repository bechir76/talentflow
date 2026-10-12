package com.talentflow.application.application;

import java.time.Clock;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.talentflow.application.domain.JobApplication;
import com.talentflow.application.domain.JobApplicationRepository;
import com.talentflow.candidate.application.CandidateService;
import com.talentflow.candidate.domain.Candidate;
import com.talentflow.candidate.domain.ConsentType;
import com.talentflow.job.application.JobService;
import com.talentflow.shared.application.CurrentUserProvider;
import com.talentflow.shared.domain.BusinessRuleException;
import com.talentflow.shared.domain.ConflictException;

/**
 * A signed-in candidate applies for a published job with a CV and explicit consent.
 */
@Service
@Transactional
public class ApplyForJobService {

    private final JobService jobs;
    private final CandidateService candidates;
    private final JobApplicationRepository applications;
    private final CvStorage cvStorage;
    private final CurrentUserProvider currentUser;
    private final Clock clock;

    public ApplyForJobService(JobService jobs, CandidateService candidates, JobApplicationRepository applications,
            CvStorage cvStorage, CurrentUserProvider currentUser, Clock clock) {
        this.jobs = jobs;
        this.candidates = candidates;
        this.applications = applications;
        this.cvStorage = cvStorage;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    public JobApplication apply(UUID jobId, ApplyCommand command) {
        if (!command.consentGiven()) {
            throw new BusinessRuleException("Consent to the processing of your data is required to apply.");
        }
        jobs.getPublished(jobId);
        Candidate candidate = candidates.registerApplicant(currentUser.currentUser(), blankToNull(command.phone()),
                blankToNull(command.location()));
        if (applications.existsByJobIdAndCandidateId(jobId, candidate.getId())) {
            throw new ConflictException("You have already applied for this job.");
        }

        String cvKey = cvStorage.store(command.cv().content());
        deleteCvIfTransactionRollsBack(cvKey);
        JobApplication application = applications.save(
                JobApplication.submit(jobId, candidate.getId(), cvKey, command.coverLetter(), clock.instant()));
        candidates.recordConsent(candidate, ConsentType.APPLICATION_PROCESSING);
        return application;
    }

    /** The file is written before the database commit; remove it again if the commit does not happen. */
    private void deleteCvIfTransactionRollsBack(String cvKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    cvStorage.delete(cvKey);
                }
            }
        });
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
