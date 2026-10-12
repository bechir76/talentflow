package com.talentflow.application.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.talentflow.application.TestFiles;
import com.talentflow.application.domain.AiStatus;
import com.talentflow.application.domain.ApplicationStage;
import com.talentflow.application.domain.CvFile;
import com.talentflow.application.domain.JobApplication;
import com.talentflow.application.domain.JobApplicationRepository;
import com.talentflow.candidate.application.CandidateService;
import com.talentflow.candidate.domain.Candidate;
import com.talentflow.candidate.domain.ConsentType;
import com.talentflow.job.application.JobService;
import com.talentflow.shared.application.CurrentUser;
import com.talentflow.shared.application.CurrentUserProvider;
import com.talentflow.shared.domain.BusinessRuleException;
import com.talentflow.shared.domain.ConflictException;
import com.talentflow.shared.domain.NotFoundException;
import com.talentflow.shared.domain.Role;

@ExtendWith(MockitoExtension.class)
class ApplyForJobServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-01T10:00:00Z");
    private static final UUID JOB_ID = UUID.randomUUID();
    private static final CurrentUser CANDIDATE =
            new CurrentUser("kc-7", "clara@example.org", "Clara Candidate", Set.of(Role.CANDIDATE));

    @Mock
    private JobService jobs;
    @Mock
    private CandidateService candidates;
    @Mock
    private JobApplicationRepository applications;
    @Mock
    private CvStorage cvStorage;
    @Mock
    private CurrentUserProvider currentUser;
    @Mock
    private ApplicationEventPublisher events;

    private ApplyForJobService service;
    private final Candidate candidate = Candidate.register("kc-7", "Clara Candidate", "clara@example.org", NOW);

    @BeforeEach
    void setUp() {
        service = new ApplyForJobService(jobs, candidates, applications, cvStorage, currentUser, events,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void submitsApplicationStoresCvAndRecordsConsent() {
        when(currentUser.currentUser()).thenReturn(CANDIDATE);
        when(candidates.registerApplicant(CANDIDATE, "+49 30 123", null)).thenReturn(candidate);
        when(cvStorage.store(any())).thenReturn("cv-key.pdf");
        when(applications.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplication application = service.apply(JOB_ID, command(true, " +49 30 123 ", "  "));

        assertThat(application.getJobId()).isEqualTo(JOB_ID);
        assertThat(application.getCandidateId()).isEqualTo(candidate.getId());
        assertThat(application.getCvStorageKey()).isEqualTo("cv-key.pdf");
        assertThat(application.getStage()).isEqualTo(ApplicationStage.APPLIED);
        assertThat(application.getAiStatus()).isEqualTo(AiStatus.PENDING);
        verify(candidates).recordConsent(candidate, ConsentType.APPLICATION_PROCESSING);
        verify(events).publishEvent(new ScreeningRequested(application.getId()));
    }

    @Test
    void requiresExplicitConsent() {
        assertThatThrownBy(() -> service.apply(JOB_ID, command(false, null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Consent");
        verifyNoInteractions(cvStorage, applications, candidates);
    }

    @Test
    void rejectsSecondApplicationForSameJobWithoutStoringTheCv() {
        when(currentUser.currentUser()).thenReturn(CANDIDATE);
        when(candidates.registerApplicant(any(), any(), any())).thenReturn(candidate);
        when(applications.existsByJobIdAndCandidateId(JOB_ID, candidate.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.apply(JOB_ID, command(true, null, null)))
                .isInstanceOf(ConflictException.class);
        verifyNoInteractions(cvStorage);
        verify(applications, never()).save(any());
    }

    @Test
    void rejectsApplicationsForUnpublishedJobs() {
        when(jobs.getPublished(JOB_ID)).thenThrow(new NotFoundException("Job", JOB_ID));

        assertThatThrownBy(() -> service.apply(JOB_ID, command(true, null, null)))
                .isInstanceOf(NotFoundException.class);
        verifyNoInteractions(cvStorage, candidates);
    }

    private static ApplyCommand command(boolean consent, String phone, String location) {
        return new ApplyCommand(phone, location, "I would love to join.", consent, CvFile.of(TestFiles.MINIMAL_PDF));
    }
}
