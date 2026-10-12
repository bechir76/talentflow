package com.talentflow.gdpr.application;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentflow.application.domain.JobApplication;
import com.talentflow.application.domain.JobApplicationRepository;
import com.talentflow.audit.application.AuditService;
import com.talentflow.audit.domain.AuditAction;
import com.talentflow.candidate.application.CandidateService;
import com.talentflow.candidate.domain.Candidate;
import com.talentflow.gdpr.application.CandidateDataExport.AiScreening;
import com.talentflow.gdpr.application.CandidateDataExport.ApplicationRecord;
import com.talentflow.gdpr.application.CandidateDataExport.ConsentRecord;
import com.talentflow.gdpr.application.CandidateDataExport.Profile;
import com.talentflow.gdpr.application.CandidateDataExport.StageRecord;
import com.talentflow.job.application.JobService;
import com.talentflow.job.application.JobView;
import com.talentflow.pipeline.domain.StageChangeRepository;
import com.talentflow.shared.application.CurrentUserProvider;
import com.talentflow.shared.domain.NotFoundException;

/**
 * Self-service data rights of a signed-in candidate: access (Art. 15) and erasure (Art. 17).
 */
@Service
@Transactional
public class MyDataService {

    static final String AI_NOTICE = "AI suggestions only support recruiters. Every decision is made by a person.";

    private final CandidateService candidates;
    private final JobApplicationRepository applications;
    private final StageChangeRepository stageChanges;
    private final JobService jobs;
    private final CandidateErasureService erasure;
    private final AuditService audit;
    private final CurrentUserProvider currentUser;
    private final Clock clock;

    public MyDataService(CandidateService candidates, JobApplicationRepository applications,
            StageChangeRepository stageChanges, JobService jobs,
            CandidateErasureService erasure, AuditService audit, CurrentUserProvider currentUser, Clock clock) {
        this.candidates = candidates;
        this.applications = applications;
        this.stageChanges = stageChanges;
        this.jobs = jobs;
        this.erasure = erasure;
        this.audit = audit;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    public CandidateDataExport export() {
        Candidate candidate = myProfile();
        List<JobApplication> myApplications = applications.findAllByCandidateId(candidate.getId());
        List<UUID> jobIds = myApplications.stream().map(JobApplication::getJobId).distinct().toList();
        Map<UUID, JobView> jobViews = jobIds.isEmpty() ? Map.of() : jobs.findViews(jobIds);

        List<ApplicationRecord> records = myApplications.stream().map(application -> new ApplicationRecord(
                application.getId(),
                jobViews.containsKey(application.getJobId()) ? jobViews.get(application.getJobId()).job().getTitle() : null,
                application.getStage().name(), application.getAppliedAt(), application.getCoverLetter(),
                application.getCvStorageKey() != null,
                new AiScreening(application.getAiStatus().name(), application.getAiScore(),
                        application.getAiMatchingSkills(), application.getAiMissingSkills(),
                        application.getAiYearsOfExperience(), application.getAiSummary(), application.getAiModel(),
                        application.getAiAnalyzedAt(), AI_NOTICE),
                stageChanges.findByApplicationId(application.getId()).stream()
                        .map(change -> new StageRecord(change.getFromStage().name(), change.getToStage().name(),
                                change.getChangedAt()))
                        .toList()))
                .toList();

        audit.record(AuditAction.DATA_EXPORTED, "Candidate", candidate.getId(), null);
        return new CandidateDataExport(clock.instant(),
                new Profile(candidate.getId(), candidate.getFullName(), candidate.getEmail(), candidate.getPhone(),
                        candidate.getLocation(), candidate.getCreatedAt()),
                candidates.consents(candidate.getId()).stream()
                        .map(c -> new ConsentRecord(c.getType().name(), c.getPolicyVersion(), c.getGivenAt(),
                                c.getWithdrawnAt()))
                        .toList(),
                records);
    }

    @Transactional(readOnly = true)
    public List<ConsentRecord> consents() {
        return candidates.findByUser(currentUser.currentUser())
                .map(candidate -> candidates.consents(candidate.getId()).stream()
                        .map(c -> new ConsentRecord(c.getType().name(), c.getPolicyVersion(), c.getGivenAt(),
                                c.getWithdrawnAt()))
                        .toList())
                .orElse(List.of());
    }

    /** Anonymises the candidate's data; the login account itself is managed by the identity provider. */
    public void deleteMyData() {
        Candidate candidate = myProfile();
        erasure.erase(candidate.getId(), clock.instant());
        audit.record(AuditAction.CANDIDATE_DELETED, "Candidate", candidate.getId(), "Requested by the candidate");
    }

    private Candidate myProfile() {
        var user = currentUser.currentUser();
        return candidates.findByUser(user).orElseThrow(() -> new NotFoundException("Candidate profile of user", user.id()));
    }
}
