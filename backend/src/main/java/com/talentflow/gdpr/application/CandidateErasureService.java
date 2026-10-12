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
import com.talentflow.application.domain.JobApplication;
import com.talentflow.application.domain.JobApplicationRepository;
import com.talentflow.candidate.application.CandidateService;
import com.talentflow.candidate.domain.Candidate;
import com.talentflow.candidate.domain.Consent;
import com.talentflow.interview.application.InterviewService;

/**
 * Anonymises a candidate and everything personal attached to them (right to erasure, Art. 17).
 * Shared by the candidate's own deletion request and the retention job. Auditing is done by the caller,
 * who knows who triggered the erasure.
 */
@Service
public class CandidateErasureService {

    private static final Logger log = LoggerFactory.getLogger(CandidateErasureService.class);

    private final CandidateService candidates;
    private final JobApplicationRepository applications;
    private final CvStorage cvStorage;
    private final InterviewService interviews;

    public CandidateErasureService(CandidateService candidates, JobApplicationRepository applications,
            CvStorage cvStorage, InterviewService interviews) {
        this.candidates = candidates;
        this.applications = applications;
        this.cvStorage = cvStorage;
        this.interviews = interviews;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void erase(UUID candidateId, Instant now) {
        Candidate candidate = candidates.get(candidateId);
        List<String> cvKeys = new ArrayList<>();
        for (JobApplication application : applications.findAllByCandidateId(candidateId)) {
            if (application.getCvStorageKey() != null) {
                cvKeys.add(application.getCvStorageKey());
            }
            application.anonymize();
            interviews.anonymizeForApplication(application.getId());
        }
        for (Consent consent : candidates.consents(candidateId)) {
            consent.withdraw(now);
        }
        candidate.anonymize(now);
        deleteFilesAfterCommit(cvKeys);
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
