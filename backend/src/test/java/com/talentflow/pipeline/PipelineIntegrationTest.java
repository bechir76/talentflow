package com.talentflow.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.talentflow.TestcontainersConfiguration;
import com.talentflow.application.TestFiles;
import com.talentflow.application.application.ApplyCommand;
import com.talentflow.application.application.ApplyForJobService;
import com.talentflow.application.domain.ApplicationStage;
import com.talentflow.application.domain.CvFile;
import com.talentflow.job.application.JobService;
import com.talentflow.job.domain.JobDetails;
import com.talentflow.job.domain.JobStatus;
import com.talentflow.job.domain.WorkMode;
import com.talentflow.pipeline.application.CandidateNotifier;
import com.talentflow.pipeline.application.DashboardService;
import com.talentflow.pipeline.application.PipelineService;
import com.talentflow.shared.application.CurrentUser;
import com.talentflow.shared.application.CurrentUserProvider;
import com.talentflow.shared.domain.Role;

/**
 * A candidate goes from applied to hired: history, audit log, emails and dashboard figures.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class PipelineIntegrationTest {

    private static final CurrentUser RECRUITER =
            new CurrentUser("kc-anna", "anna@example.org", "Anna Keller", Set.of(Role.RECRUITER));

    @Autowired
    private JobService jobService;
    @Autowired
    private ApplyForJobService applyForJob;
    @Autowired
    private PipelineService pipeline;
    @Autowired
    private DashboardService dashboard;
    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private CurrentUserProvider currentUser;
    @MockitoBean
    private CandidateNotifier notifier;

    private UUID jobId;
    private String candidateEmail;

    @BeforeEach
    void setUp() {
        UUID departmentId = jdbc.queryForObject("SELECT id FROM department WHERE name = 'Data'", UUID.class);
        when(currentUser.currentUser()).thenReturn(RECRUITER);
        jobId = jobService.create(new JobDetails("Data Engineer " + UUID.randomUUID(),
                "Build data pipelines for our analytics platform. ".repeat(3), List.of("SQL", "Python"),
                "Hamburg", WorkMode.REMOTE, null, null, departmentId), JobStatus.PUBLISHED).job().getId();
        String userId = "kc-" + UUID.randomUUID();
        candidateEmail = userId + "@example.org";
        when(currentUser.currentUser()).thenReturn(
                new CurrentUser(userId, candidateEmail, "Clara Candidate", Set.of(Role.CANDIDATE)));
    }

    @Test
    void movesCandidateToHiredWithHistoryAuditEmailsAndDashboard() {
        UUID applicationId = applyForJob.apply(jobId, new ApplyCommand(null, null, null, true,
                CvFile.of(TestFiles.pdfWithText("SQL and Python, 4 years")))).getId();
        when(currentUser.currentUser()).thenReturn(RECRUITER);

        for (ApplicationStage stage : List.of(ApplicationStage.SCREENING, ApplicationStage.INTERVIEW,
                ApplicationStage.OFFER, ApplicationStage.HIRED)) {
            pipeline.moveStage(applicationId, stage, null);
        }

        assertThat(pipeline.history(applicationId)).extracting(change -> change.getToStage())
                .containsExactly(ApplicationStage.HIRED, ApplicationStage.OFFER, ApplicationStage.INTERVIEW,
                        ApplicationStage.SCREENING);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM audit_log WHERE entity_id = ? AND action = 'STAGE_CHANGED'"
                + " AND actor_id = 'kc-anna'", Integer.class, applicationId)).isEqualTo(4);
        verify(notifier, timeout(5000)).stageChanged(eq(candidateEmail), eq("Clara Candidate"), anyString(),
                eq(ApplicationStage.HIRED));

        var board = pipeline.board(jobId, PageRequest.of(0, 100));
        assertThat(board.getContent()).singleElement().satisfies(card -> {
            assertThat(card.candidate().getFullName()).isEqualTo("Clara Candidate");
            assertThat(card.application().getStage()).isEqualTo(ApplicationStage.HIRED);
        });

        var figures = dashboard.dashboard();
        assertThat(figures.funnel()).extracting(DashboardService.FunnelStep::stage).containsExactly(
                ApplicationStage.APPLIED, ApplicationStage.SCREENING, ApplicationStage.INTERVIEW,
                ApplicationStage.OFFER, ApplicationStage.HIRED);
        assertThat(figures.funnel().getLast().applications()).isPositive();
        assertThat(figures.averageDaysToHire()).isNotNull().isGreaterThanOrEqualTo(0.0);
        assertThat(figures.applicationsPerJob()).anySatisfy(row -> assertThat(row.jobId()).isEqualTo(jobId));
    }

    @Test
    void rejectedCandidatesStillCountForTheStagesTheyReached() {
        UUID applicationId = applyForJob.apply(jobId, new ApplyCommand(null, null, null, true,
                CvFile.of(TestFiles.MINIMAL_PDF))).getId();
        when(currentUser.currentUser()).thenReturn(RECRUITER);
        long interviewsBefore = interviewCount();

        pipeline.moveStage(applicationId, ApplicationStage.INTERVIEW, null);
        pipeline.moveStage(applicationId, ApplicationStage.REJECTED, "Not a fit for the team setup");

        assertThat(interviewCount()).isEqualTo(interviewsBefore + 1);
    }

    private long interviewCount() {
        return dashboard.dashboard().funnel().stream()
                .filter(step -> step.stage() == ApplicationStage.INTERVIEW).findFirst().orElseThrow().applications();
    }
}
