package com.talentflow.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import com.talentflow.application.application.ApplyCommand;
import com.talentflow.application.application.ApplyForJobService;
import com.talentflow.application.application.CvStorage;
import com.talentflow.application.application.MyApplicationsService;
import com.talentflow.application.domain.CvFile;
import com.talentflow.application.domain.JobApplication;
import com.talentflow.job.application.JobService;
import com.talentflow.job.domain.JobDetails;
import com.talentflow.job.domain.JobStatus;
import com.talentflow.job.domain.WorkMode;
import com.talentflow.shared.application.CurrentUser;
import com.talentflow.shared.application.CurrentUserProvider;
import com.talentflow.shared.domain.ConflictException;
import com.talentflow.shared.domain.Role;

/**
 * Runs the apply flow against PostgreSQL: job, candidate, consent, application and CV file together.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ApplyForJobIntegrationTest {

    @Autowired
    private JobService jobService;
    @Autowired
    private ApplyForJobService applyForJob;
    @Autowired
    private MyApplicationsService myApplications;
    @Autowired
    private CvStorage cvStorage;
    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private CurrentUserProvider currentUser;

    private UUID jobId;
    private CurrentUser candidate;

    @BeforeEach
    void setUp() {
        UUID departmentId = jdbc.queryForObject("SELECT id FROM department WHERE name = 'Engineering'", UUID.class);
        when(currentUser.currentUser()).thenReturn(
                new CurrentUser("kc-recruiter", "r@example.org", "Rita Recruiter", Set.of(Role.RECRUITER)));
        jobId = jobService.create(new JobDetails("Backend Engineer " + UUID.randomUUID(),
                "Build reliable services for our platform. ".repeat(3), List.of("Java"), "Berlin", WorkMode.REMOTE,
                null, null, departmentId), JobStatus.PUBLISHED).job().getId();

        String userId = "kc-" + UUID.randomUUID();
        candidate = new CurrentUser(userId, userId + "@example.org", "Clara Candidate", Set.of(Role.CANDIDATE));
        when(currentUser.currentUser()).thenReturn(candidate);
    }

    @Test
    void applicationIsStoredWithConsentAndListedInThePortal() {
        JobApplication application = applyForJob.apply(jobId, command());

        assertThat(cvStorage.load(application.getCvStorageKey())).startsWith(TestFiles.MINIMAL_PDF);
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM consent c JOIN candidate cd ON cd.id = c.candidate_id
                WHERE cd.keycloak_user_id = ? AND c.type = 'APPLICATION_PROCESSING' AND c.policy_version = '2026-10'
                """, Integer.class, candidate.id())).isEqualTo(1);

        var page = myApplications.list(PageRequest.of(0, 10));
        assertThat(page.getContent()).singleElement()
                .satisfies(item -> assertThat(item.job().job().getId()).isEqualTo(jobId));
        assertThat(jobService.findViews(List.of(jobId)).get(jobId).applicantCount()).isEqualTo(1);
    }

    @Test
    void secondApplicationForTheSameJobIsRejected() {
        applyForJob.apply(jobId, command());

        assertThatThrownBy(() -> applyForJob.apply(jobId, command())).isInstanceOf(ConflictException.class);
    }

    @Test
    void candidateWithoutApplicationsSeesEmptyPortal() {
        assertThat(myApplications.list(PageRequest.of(0, 10))).isEmpty();
    }

    private static ApplyCommand command() {
        return new ApplyCommand("+49 30 123", "Berlin", null, true, CvFile.of(TestFiles.MINIMAL_PDF));
    }
}
