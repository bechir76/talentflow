package com.talentflow.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Map;
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
import com.talentflow.application.application.ApplyCommand;
import com.talentflow.application.application.ApplyForJobService;
import com.talentflow.application.domain.CvFile;
import com.talentflow.job.application.JobService;
import com.talentflow.job.domain.JobDetails;
import com.talentflow.job.domain.JobStatus;
import com.talentflow.job.domain.WorkMode;
import com.talentflow.shared.application.CurrentUser;
import com.talentflow.shared.application.CurrentUserProvider;
import com.talentflow.shared.domain.Role;

/**
 * Applying triggers the asynchronous screening after commit. Without an API key the keyword analyzer runs.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ScreeningIntegrationTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    @Autowired
    private JobService jobService;
    @Autowired
    private ApplyForJobService applyForJob;
    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private CurrentUserProvider currentUser;

    private UUID jobId;

    @BeforeEach
    void setUp() {
        UUID departmentId = jdbc.queryForObject("SELECT id FROM department WHERE name = 'Engineering'", UUID.class);
        when(currentUser.currentUser()).thenReturn(new CurrentUser("kc-r", "r@example.org", "Rita", Set.of(Role.RECRUITER)));
        jobId = jobService.create(new JobDetails("Java Developer " + UUID.randomUUID(),
                "Build reliable services for our platform. ".repeat(3), List.of("Java", "Spring Boot", "Kubernetes"),
                "Berlin", WorkMode.HYBRID, null, null, departmentId), JobStatus.PUBLISHED).job().getId();
        String userId = "kc-" + UUID.randomUUID();
        when(currentUser.currentUser()).thenReturn(
                new CurrentUser(userId, userId + "@example.org", "Lena Schmidt", Set.of(Role.CANDIDATE)));
    }

    @Test
    void screensApplicationInTheBackgroundAfterApplying() throws InterruptedException {
        byte[] cv = TestFiles.pdfWithText("Lena Schmidt - Backend Engineer",
                "6 years of experience with Java and Spring Boot.");
        UUID applicationId = applyForJob.apply(jobId, command(cv)).getId();

        Map<String, Object> row = awaitScreening(applicationId);

        assertThat(row.get("ai_status")).isEqualTo("DONE");
        assertThat(row.get("ai_model")).isEqualTo("keyword-matcher-v1");
        assertThat((Integer) row.get("ai_score")).isBetween(1, 100);
        assertThat(row.get("ai_summary").toString()).contains("2 of 3").doesNotContain("Lena");
    }

    @Test
    void marksScreeningFailedWhenTheCvHasNoText() throws InterruptedException {
        UUID applicationId = applyForJob.apply(jobId, command(TestFiles.MINIMAL_PDF)).getId();

        assertThat(awaitScreening(applicationId).get("ai_status")).isEqualTo("FAILED");
        assertThat(jdbc.queryForObject("SELECT stage FROM job_application WHERE id = ?", String.class, applicationId))
                .isEqualTo("APPLIED");
    }

    private Map<String, Object> awaitScreening(UUID applicationId) throws InterruptedException {
        long deadline = System.nanoTime() + TIMEOUT.toNanos();
        while (System.nanoTime() < deadline) {
            Map<String, Object> row = jdbc.queryForMap(
                    "SELECT ai_status, ai_score, ai_summary, ai_model FROM job_application WHERE id = ?", applicationId);
            if (!"PENDING".equals(row.get("ai_status"))) {
                return row;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Screening did not finish within " + TIMEOUT);
    }

    private static ApplyCommand command(byte[] cv) {
        return new ApplyCommand(null, null, null, true, CvFile.of(cv));
    }
}
