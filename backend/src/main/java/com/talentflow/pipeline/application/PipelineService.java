package com.talentflow.pipeline.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentflow.application.application.ApplicationAccessGuard;
import com.talentflow.application.domain.ApplicationStage;
import com.talentflow.application.domain.JobApplication;
import com.talentflow.application.domain.JobApplicationRepository;
import com.talentflow.audit.application.AuditService;
import com.talentflow.audit.domain.AuditAction;
import com.talentflow.candidate.application.CandidateService;
import com.talentflow.candidate.domain.Candidate;
import com.talentflow.pipeline.domain.StageChange;
import com.talentflow.pipeline.domain.StageChangeRepository;
import com.talentflow.shared.application.CurrentUser;
import com.talentflow.shared.application.CurrentUserProvider;
import com.talentflow.shared.domain.NotFoundException;

/**
 * Moves applications through the hiring pipeline and shows the Kanban board.
 */
@Service
@Transactional
public class PipelineService {

    private final JobApplicationRepository applications;
    private final StageChangeRepository stageChanges;
    private final CandidateService candidates;
    private final AuditService audit;
    private final CurrentUserProvider currentUser;
    private final ApplicationEventPublisher events;
    private final ApplicationAccessGuard access;
    private final Clock clock;

    public PipelineService(JobApplicationRepository applications, StageChangeRepository stageChanges,
            CandidateService candidates, AuditService audit, CurrentUserProvider currentUser,
            ApplicationEventPublisher events, ApplicationAccessGuard access, Clock clock) {
        this.applications = applications;
        this.stageChanges = stageChanges;
        this.candidates = candidates;
        this.audit = audit;
        this.currentUser = currentUser;
        this.events = events;
        this.access = access;
        this.clock = clock;
    }

    /** Records the move, writes an audit entry and notifies the candidate after commit. */
    public StageChange moveStage(UUID applicationId, ApplicationStage target, String note) {
        JobApplication application = applications.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application", applicationId));
        CurrentUser user = currentUser.currentUser();
        Instant now = clock.instant();
        ApplicationStage from = application.moveTo(target, now);
        StageChange change = stageChanges.save(
                StageChange.record(applicationId, from, target, user.id(), user.fullName(), note, now));
        audit.record(AuditAction.STAGE_CHANGED, "JobApplication", applicationId, from + " -> " + target);
        events.publishEvent(new StageChanged(applicationId, from, target));
        return change;
    }

    @Transactional(readOnly = true)
    public Page<PipelineCard> board(UUID jobId, Pageable pageable) {
        Page<JobApplication> page = applications.findByJobId(jobId, pageable);
        List<UUID> candidateIds = page.getContent().stream().map(JobApplication::getCandidateId).distinct().toList();
        Map<UUID, Candidate> byId = candidateIds.isEmpty() ? Map.of() : candidates.findAllById(candidateIds);
        return page.map(application -> new PipelineCard(application, byId.get(application.getCandidateId())));
    }

    @Transactional(readOnly = true)
    public List<StageChange> history(UUID applicationId) {
        access.requireAccess(applicationId);
        if (applications.findById(applicationId).isEmpty()) {
            throw new NotFoundException("Application", applicationId);
        }
        return stageChanges.findByApplicationId(applicationId);
    }

    public record PipelineCard(JobApplication application, Candidate candidate) {
    }
}
