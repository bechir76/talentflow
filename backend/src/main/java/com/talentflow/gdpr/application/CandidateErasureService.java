package com.talentflow.gdpr.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.talentflow.application.application.CvStorage;
import com.talentflow.application.domain.ApplicationStage;
import com.talentflow.application.domain.JobApplication;
import com.talentflow.application.domain.JobApplicationRepository;
import com.talentflow.candidate.application.CandidateService;
import com.talentflow.candidate.domain.Candidate;
import com.talentflow.candidate.domain.Consent;
import com.talentflow.interview.application.InterviewService;
import com.talentflow.pipeline.domain.StageChange;
import com.talentflow.pipeline.domain.StageChangeRepository;

/**
 * Anonymises a candidate and everything personal attached to them (right to erasure, Art. 17).
 * Shared by the candidate's own deletion request and the retention job. Auditing is done by the caller,
 * who knows who triggered the erasure.
 */
@Service
public class CandidateErasureService {

    static final String WITHDRAWN_NOTE = "Application withdrawn: the candidate's data was erased";
    private static final Logger log = LoggerFactory.getLogger(CandidateErasureService.class);

    private final CandidateService candidates;
    private final JobApplicationRepository applications;
    private final CvStorage cvStorage;
    private final InterviewService interviews;
    private final StageChangeRepository stageChanges;

    public CandidateErasureService(CandidateService candidates, JobApplicationRepository applications,
            CvStorage cvStorage, InterviewService interviews, StageChangeRepository stageChanges) {
        this.candidates = candidates;
        this.applications = applications;
        this.cvStorage = cvStorage;
        this.interviews = interviews;
        this.stageChanges = stageChanges;
    }

    /**
     * @param actorId   who triggered the erasure (the candidate's user id or the retention job)
     * @param actorName display name for the stage history of applications that are ended
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void erase(UUID candidateId, Instant now, String actorId, String actorName) {
        Candidate candidate = candidates.get(candidateId);
        List<String> cvKeys = new ArrayList<>();
        for (JobApplication application : applications.findAllByCandidateId(candidateId)) {
            if (application.getCvStorageKey() != null) {
                cvKeys.add(application.getCvStorageKey());
            }
            endIfActive(application, now, actorId, actorName);
            application.anonymize();
            interviews.anonymizeForApplication(application.getId());
        }
        for (Consent consent : candidates.consents(candidateId)) {
            consent.withdraw(now);
        }
        candidate.anonymize(now);
        deleteFilesAfterCommit(cvKeys);
    }

    /** Open applications end with the erasure: there is no longer a candidate to continue with. */
    private void endIfActive(JobApplication application, Instant now, String actorId, String actorName) {
        if (application.getStage().isActive()) {
            ApplicationStage from = application.moveTo(ApplicationStage.REJECTED, now);
            stageChanges.save(StageChange.record(application.getId(), from, ApplicationStage.REJECTED, actorId,
                    actorName, WITHDRAWN_NOTE, now));
        }
    }

    /** Files are deleted only once the anonymisation is committed, so a rollback never loses a CV. */
    private void deleteFilesAfterCommit(List<String> cvKeys) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (String key : cvKeys) {
                    try {
                        cvStorage.delete(key);
                    } catch (RuntimeException e) {
                        log.error("Could not delete CV file {} after erasure", key, e);
                    }
                }
            }
        });
    }
}
