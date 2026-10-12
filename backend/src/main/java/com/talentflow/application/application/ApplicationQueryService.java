package com.talentflow.application.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentflow.application.domain.JobApplication;
import com.talentflow.application.domain.JobApplicationRepository;
import com.talentflow.candidate.application.CandidateService;
import com.talentflow.candidate.domain.Candidate;
import com.talentflow.candidate.domain.Consent;
import com.talentflow.job.application.JobService;
import com.talentflow.job.application.JobView;
import com.talentflow.shared.domain.NotFoundException;

/**
 * Read side for staff: one application with its candidate, job, consent and CV.
 */
@Service
@Transactional(readOnly = true)
public class ApplicationQueryService {

    private final JobApplicationRepository applications;
    private final CandidateService candidates;
    private final JobService jobs;
    private final CvStorage cvStorage;

    public ApplicationQueryService(JobApplicationRepository applications, CandidateService candidates,
            JobService jobs, CvStorage cvStorage) {
        this.applications = applications;
        this.candidates = candidates;
        this.jobs = jobs;
        this.cvStorage = cvStorage;
    }

    public ApplicationDetails get(UUID id) {
        JobApplication application = load(id);
        Candidate candidate = candidates.get(application.getCandidateId());
        JobView job = jobs.findViews(List.of(application.getJobId())).get(application.getJobId());
        return new ApplicationDetails(application, candidate, job,
                candidates.latestApplicationConsent(candidate.getId()));
    }

    public byte[] cv(UUID id) {
        JobApplication application = load(id);
        if (application.getCvStorageKey() == null) {
            throw new NotFoundException("CV of application", id);
        }
        return cvStorage.load(application.getCvStorageKey());
    }

    private JobApplication load(UUID id) {
        return applications.findById(id).orElseThrow(() -> new NotFoundException("Application", id));
    }

    public record ApplicationDetails(JobApplication application, Candidate candidate, JobView job,
            Optional<Consent> consent) {
    }
}
