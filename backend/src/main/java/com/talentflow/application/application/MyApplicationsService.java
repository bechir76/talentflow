package com.talentflow.application.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentflow.application.domain.JobApplication;
import com.talentflow.application.domain.JobApplicationRepository;
import com.talentflow.candidate.application.CandidateService;
import com.talentflow.job.application.JobService;
import com.talentflow.job.application.JobView;
import com.talentflow.shared.application.CurrentUserProvider;

/**
 * The signed-in candidate's own applications, for the candidate portal.
 */
@Service
@Transactional(readOnly = true)
public class MyApplicationsService {

    private final CandidateService candidates;
    private final JobApplicationRepository applications;
    private final JobService jobs;
    private final CurrentUserProvider currentUser;

    public MyApplicationsService(CandidateService candidates, JobApplicationRepository applications, JobService jobs,
            CurrentUserProvider currentUser) {
        this.candidates = candidates;
        this.applications = applications;
        this.jobs = jobs;
        this.currentUser = currentUser;
    }

    public Page<MyApplication> list(Pageable pageable) {
        return candidates.findByUser(currentUser.currentUser())
                .map(candidate -> withJobs(applications.findByCandidateId(candidate.getId(), pageable)))
                .orElseGet(() -> Page.empty(pageable));
    }

    private Page<MyApplication> withJobs(Page<JobApplication> page) {
        List<UUID> jobIds = page.getContent().stream().map(JobApplication::getJobId).distinct().toList();
        Map<UUID, JobView> jobViews = jobIds.isEmpty() ? Map.of() : jobs.findViews(jobIds);
        return page.map(application -> new MyApplication(application, jobViews.get(application.getJobId())));
    }

    public record MyApplication(JobApplication application, JobView job) {
    }
}
