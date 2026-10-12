package com.talentflow.pipeline.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.talentflow.application.domain.ApplicationStage;
import com.talentflow.application.domain.JobApplication;
import com.talentflow.application.domain.JobApplicationRepository;
import com.talentflow.audit.application.AuditService;
import com.talentflow.audit.domain.AuditAction;
import com.talentflow.candidate.application.CandidateService;
import com.talentflow.pipeline.domain.StageChange;
import com.talentflow.pipeline.domain.StageChangeRepository;
import com.talentflow.shared.application.CurrentUser;
import com.talentflow.shared.application.CurrentUserProvider;
import com.talentflow.shared.domain.BusinessRuleException;
import com.talentflow.shared.domain.NotFoundException;
import com.talentflow.shared.domain.Role;

@ExtendWith(MockitoExtension.class)
class PipelineServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-05T09:00:00Z");

    @Mock
    private JobApplicationRepository applications;
    @Mock
    private StageChangeRepository stageChanges;
    @Mock
    private CandidateService candidates;
    @Mock
    private AuditService audit;
    @Mock
    private CurrentUserProvider currentUser;
    @Mock
    private ApplicationEventPublisher events;

    private PipelineService service;
    private final JobApplication application = JobApplication.submit(UUID.randomUUID(), UUID.randomUUID(), "cv.pdf",
            null, Instant.parse("2026-10-01T10:00:00Z"));

    @BeforeEach
    void setUp() {
        service = new PipelineService(applications, stageChanges, candidates, audit, currentUser, events,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void movesStageRecordsHistoryAuditsAndNotifies() {
        when(applications.findById(application.getId())).thenReturn(Optional.of(application));
        when(currentUser.currentUser()).thenReturn(new CurrentUser("kc-anna", null, "Anna Keller", Set.of(Role.RECRUITER)));
        when(stageChanges.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StageChange change = service.moveStage(application.getId(), ApplicationStage.SCREENING, " Strong CV ");

        assertThat(application.getStage()).isEqualTo(ApplicationStage.SCREENING);
        assertThat(change.getFromStage()).isEqualTo(ApplicationStage.APPLIED);
        assertThat(change.getChangedByName()).isEqualTo("Anna Keller");
        assertThat(change.getNote()).isEqualTo("Strong CV");
        assertThat(change.getChangedAt()).isEqualTo(NOW);
        verify(audit).record(AuditAction.STAGE_CHANGED, "JobApplication", application.getId(), "APPLIED -> SCREENING");
        verify(events).publishEvent(new StageChanged(application.getId(), ApplicationStage.APPLIED, ApplicationStage.SCREENING));
    }

    @Test
    void doesNothingWhenTheMoveBreaksARule() {
        when(applications.findById(application.getId())).thenReturn(Optional.of(application));
        when(currentUser.currentUser()).thenReturn(new CurrentUser("kc-anna", null, "Anna", Set.of(Role.RECRUITER)));

        assertThatThrownBy(() -> service.moveStage(application.getId(), ApplicationStage.HIRED, null))
                .isInstanceOf(BusinessRuleException.class);
        verifyNoInteractions(stageChanges, audit, events);
    }

    @Test
    void reportsUnknownApplication() {
        UUID id = UUID.randomUUID();
        when(applications.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.moveStage(id, ApplicationStage.SCREENING, null))
                .isInstanceOf(NotFoundException.class);
    }
}
