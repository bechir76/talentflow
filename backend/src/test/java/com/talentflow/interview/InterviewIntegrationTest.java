package com.talentflow.interview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.talentflow.TestcontainersConfiguration;
import com.talentflow.application.TestFiles;
import com.talentflow.application.application.ApplicationQueryService;
import com.talentflow.application.application.ApplyCommand;
import com.talentflow.application.application.ApplyForJobService;
import com.talentflow.application.domain.CvFile;
import com.talentflow.interview.application.InterviewInvitationSender;
import com.talentflow.interview.application.InterviewService;
import com.talentflow.interview.domain.Feedback.Scores;
import com.talentflow.interview.domain.Recommendation;
import com.talentflow.job.application.JobService;
import com.talentflow.job.domain.JobDetails;
import com.talentflow.job.domain.JobStatus;
import com.talentflow.job.domain.WorkMode;
import com.talentflow.pipeline.application.DashboardService;
import com.talentflow.shared.application.CurrentUser;
import com.talentflow.shared.application.CurrentUserProvider;
import com.talentflow.shared.domain.ConflictException;
import com.talentflow.shared.domain.ForbiddenException;
import com.talentflow.shared.domain.Role;
import com.talentflow.user.application.UserAccount;
import com.talentflow.user.application.UserDirectory;

/**
 * Scheduling, the per-role calendar, feedback rules and the hiring manager's access to assigned candidates.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class InterviewIntegrationTest {

    private static final CurrentUser RECRUITER = new CurrentUser("kc-anna", "anna@x.dev", "Anna Keller", Set.of(Role.RECRUITER));
    private static final CurrentUser MANAGER = new CurrentUser("kc-martin", "martin@x.dev", "Martin Richter", Set.of(Role.HIRING_MANAGER));
    private static final CurrentUser OTHER_MANAGER = new CurrentUser("kc-olga", "olga@x.dev", "Olga Other", Set.of(Role.HIRING_MANAGER));

    @Autowired
    private JobService jobService;
    @Autowired
    private ApplyForJobService applyForJob;
    @Autowired
    private InterviewService interviews;
    @Autowired
    private ApplicationQueryService applications;
    @Autowired
    private DashboardService dashboard;
    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private CurrentUserProvider currentUser;
    @MockitoBean
    private UserDirectory userDirectory;
    @MockitoBean
    private InterviewInvitationSender invitations;

    private UUID applicationId;
    private String candidateEmail;

    @BeforeEach
    void setUp() {
        when(userDirectory.withRole(Role.HIRING_MANAGER)).thenReturn(List.of(
                new UserAccount("kc-martin", "martin@x.dev", "Martin", "Richter", true, Set.of(Role.HIRING_MANAGER)),
                new UserAccount("kc-olga", "olga@x.dev", "Olga", "Other", true, Set.of(Role.HIRING_MANAGER))));
        when(userDirectory.withRole(Role.RECRUITER)).thenReturn(List.of(
                new UserAccount("kc-anna", "anna@x.dev", "Anna", "Keller", true, Set.of(Role.RECRUITER))));
        UUID departmentId = jdbc.queryForObject("SELECT id FROM department WHERE name = 'Engineering'", UUID.class);
        when(currentUser.currentUser()).thenReturn(RECRUITER);
        UUID jobId = jobService.create(new JobDetails("Platform Engineer " + UUID.randomUUID(),
                "Run and improve our Kubernetes platform for all teams. ".repeat(3), List.of("Kubernetes"),
                "Berlin", WorkMode.HYBRID, null, null, departmentId), JobStatus.PUBLISHED).job().getId();
        String userId = "kc-" + UUID.randomUUID();
        candidateEmail = userId + "@example.org";
        when(currentUser.currentUser()).thenReturn(new CurrentUser(userId, candidateEmail, "Clara Candidate", Set.of(Role.CANDIDATE)));
        applicationId = applyForJob.apply(jobId, new ApplyCommand(null, null, null, true,
                CvFile.of(TestFiles.pdfWithText("Kubernetes engineer")))).getId();
    }

    @Test
    void scheduleInviteGiveFeedbackAndRespectAssignments() {
        when(currentUser.currentUser()).thenReturn(RECRUITER);
        // PostgreSQL stores microseconds; truncate so the value read back equals the one sent.
        Instant start = Instant.now().plus(Duration.ofHours(2)).truncatedTo(ChronoUnit.SECONDS);
        var interview = interviews.schedule(applicationId, start, 60, List.of("kc-martin"), "https://meet.example.org/abc");
        verify(invitations, timeout(5000)).send(eq(candidateEmail), eq("Clara Candidate"), anyString(), eq(start),
                eq(60), eq("https://meet.example.org/abc"));

        when(currentUser.currentUser()).thenReturn(MANAGER);
        assertThat(applications.get(applicationId).candidate().getFullName()).isEqualTo("Clara Candidate");
        assertThat(interviews.calendar(start.minusSeconds(60), start.plusSeconds(60)))
                .extracting(entry -> entry.interview().getId()).contains(interview.getId());
        assertThat(dashboard.dashboard().upcomingInterviews())
                .anySatisfy(upcoming -> assertThat(upcoming.candidateName()).isEqualTo("Clara Candidate"));
        assertThat(dashboard.dashboard().topMatches()).as("no names of unassigned candidates").isEmpty();

        interviews.submitFeedback(interview.getId(), new Scores(5, 4, 4), "Very strong Kubernetes skills", Recommendation.HIRE);
        assertThatThrownBy(() -> interviews.submitFeedback(interview.getId(), new Scores(3, 3, 3), null, Recommendation.UNSURE))
                .isInstanceOf(ConflictException.class);
        assertThat(interviews.forApplication(applicationId)).singleElement()
                .satisfies(item -> assertThat(item.feedback()).singleElement()
                        .satisfies(f -> assertThat(f.getAuthorName()).isEqualTo("Martin Richter")));

        when(currentUser.currentUser()).thenReturn(OTHER_MANAGER);
        assertThatThrownBy(() -> applications.get(applicationId)).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> interviews.submitFeedback(interview.getId(), new Scores(1, 1, 1), null, Recommendation.NO_HIRE))
                .isInstanceOf(ForbiddenException.class);
        assertThat(interviews.calendar(start.minusSeconds(60), start.plusSeconds(60)))
                .extracting(entry -> entry.interview().getId()).doesNotContain(interview.getId());
    }

    @Test
    void onlyKnownInterviewersCanBeInvited() {
        when(currentUser.currentUser()).thenReturn(RECRUITER);

        assertThatThrownBy(() -> interviews.schedule(applicationId, Instant.now().plus(Duration.ofDays(1)), 30,
                List.of("kc-unknown"), null)).hasMessageContaining("cannot be invited");
        verify(invitations, never()).send(any(), any(), any(), any(), anyInt(), any());
    }
}
