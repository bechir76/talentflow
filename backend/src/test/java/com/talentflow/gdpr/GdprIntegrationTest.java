package com.talentflow.gdpr;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.talentflow.TestcontainersConfiguration;
import com.talentflow.application.TestFiles;
import com.talentflow.application.application.ApplicationQueryService;
import com.talentflow.application.application.ApplyCommand;
import com.talentflow.application.application.ApplyForJobService;
import com.talentflow.application.application.CvStorage;
import com.talentflow.application.domain.ApplicationStage;
import com.talentflow.application.domain.CvFile;
import com.talentflow.gdpr.application.CandidateDataExport;
import com.talentflow.gdpr.application.MyDataService;
import com.talentflow.gdpr.application.RetentionService;
import com.talentflow.job.application.JobService;
import com.talentflow.job.domain.JobDetails;
import com.talentflow.job.domain.JobStatus;
import com.talentflow.job.domain.WorkMode;
import com.talentflow.pipeline.application.CandidateNotifier;
import com.talentflow.pipeline.application.PipelineService;
import com.talentflow.shared.application.CurrentUser;
import com.talentflow.shared.application.CurrentUserProvider;
import com.talentflow.shared.domain.Role;

/**
 * Data rights end to end: export (Art. 15), erasure (Art. 17) and the retention job, including audit entries.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class GdprIntegrationTest {

    private static final CurrentUser RECRUITER = new CurrentUser("kc-rita", "r@example.org", "Rita", Set.of(Role.RECRUITER));

    @Autowired
    private JobService jobService;
    @Autowired
    private ApplyForJobService applyForJob;
    @Autowired
    private PipelineService pipeline;
    @Autowired
    private ApplicationQueryService applicationQueries;
    @Autowired
    private MyDataService myData;
    @Autowired
    private RetentionService retention;
    @Autowired
    private CvStorage cvStorage;
    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private CurrentUserProvider currentUser;
    @MockitoBean
    private CandidateNotifier notifier;

    private UUID jobId;
    private CurrentUser candidate;

    @BeforeEach
    void setUp() {
        UUID departmentId = jdbc.queryForObject("SELECT id FROM department WHERE name = 'Sales'", UUID.class);
        when(currentUser.currentUser()).thenReturn(RECRUITER);
        jobId = jobService.create(new JobDetails("Account Executive " + UUID.randomUUID(),
                "Win and grow enterprise customers across Europe. ".repeat(3), List.of("B2B Sales"), "Munich",
                WorkMode.HYBRID, null, null, departmentId), JobStatus.PUBLISHED).job().getId();
        String userId = "kc-" + UUID.randomUUID();
        candidate = new CurrentUser(userId, userId + "@example.org", "Clara Candidate", Set.of(Role.CANDIDATE));
    }

    @Test
    void exportContainsEverythingAndIsAudited() {
        UUID applicationId = apply("Please consider me.");

        when(currentUser.currentUser()).thenReturn(candidate);
        CandidateDataExport export = myData.export();

        assertThat(export.profile().email()).isEqualTo(candidate.email());
        assertThat(export.consents()).singleElement().satisfies(c -> assertThat(c.withdrawnAt()).isNull());
        assertThat(export.applications()).singleElement().satisfies(a -> {
            assertThat(a.coverLetter()).isEqualTo("Please consider me.");
            assertThat(a.aiScreening().notice()).contains("person");
        });
        assertThat(auditCount("DATA_EXPORTED", export.profile().id())).isEqualTo(1);
        assertThat(applicationId).isNotNull();
    }

    @Test
    void erasureAnonymisesTheCandidateDeletesTheCvAndKeepsStatistics() {
        UUID applicationId = apply("Personal motivation");
        String cvKey = jdbc.queryForObject("SELECT cv_storage_key FROM job_application WHERE id = ?", String.class, applicationId);
        when(currentUser.currentUser()).thenReturn(RECRUITER);
        applicationQueries.cv(applicationId, false);

        when(currentUser.currentUser()).thenReturn(candidate);
        UUID candidateId = myData.export().profile().id();
        myData.deleteMyData();

        var row = jdbc.queryForMap("SELECT full_name, email, phone, keycloak_user_id, anonymized FROM candidate WHERE id = ?", candidateId);
        assertThat(row.get("full_name")).isEqualTo("Anonymised candidate");
        assertThat(row.get("email").toString()).endsWith("@anonymised.invalid");
        assertThat(row.get("keycloak_user_id")).isNull();
        assertThat(row.get("anonymized")).isEqualTo(true);
        assertThat(jdbc.queryForMap("SELECT cv_storage_key, cover_letter, stage FROM job_application WHERE id = ?", applicationId))
                .containsEntry("cv_storage_key", null).containsEntry("cover_letter", null).containsEntry("stage", "REJECTED");
        assertThat(jdbc.queryForObject("SELECT note FROM stage_change WHERE application_id = ? AND to_stage = 'REJECTED'",
                String.class, applicationId)).contains("data was erased");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM consent WHERE candidate_id = ? AND withdrawn_at IS NULL",
                Integer.class, candidateId)).isZero();
        assertThatThrownBy(() -> cvStorage.load(cvKey)).isInstanceOf(RuntimeException.class);
        assertThat(auditCount("CANDIDATE_DELETED", candidateId)).isEqualTo(1);
        assertThat(auditCount("CV_VIEWED", applicationId)).isEqualTo(1);
    }

    @Test
    void retentionAnonymisesOnlyCandidatesRejectedLongEnoughAgo() {
        UUID oldRejection = apply(null);
        when(currentUser.currentUser()).thenReturn(RECRUITER);
        pipeline.moveStage(oldRejection, ApplicationStage.REJECTED, null);
        jdbc.update("UPDATE job_application SET stage_changed_at = now() - interval '13 months' WHERE id = ?", oldRejection);

        candidate = new CurrentUser("kc-" + UUID.randomUUID(), "fresh@example.org", "Fresh Rejection", Set.of(Role.CANDIDATE));
        UUID recentRejection = apply(null);
        when(currentUser.currentUser()).thenReturn(RECRUITER);
        pipeline.moveStage(recentRejection, ApplicationStage.REJECTED, null);
        retention.updatePolicy(12, true);

        int anonymised = retention.anonymizeExpired();

        assertThat(anonymised).isGreaterThanOrEqualTo(1);
        assertThat(isAnonymised(oldRejection)).isTrue();
        assertThat(isAnonymised(recentRejection)).isFalse();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM audit_log WHERE actor_id = 'system' AND action = 'CANDIDATE_DELETED'",
                Integer.class)).isPositive();
        retention.updatePolicy(6, false);
    }

    private UUID apply(String coverLetter) {
        when(currentUser.currentUser()).thenReturn(candidate);
        return applyForJob.apply(jobId, new ApplyCommand("+49 30 1234567", "Munich", coverLetter, true,
                CvFile.of(TestFiles.pdfWithText("B2B Sales manager")))).getId();
    }

    private boolean isAnonymised(UUID applicationId) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
                SELECT c.anonymized FROM candidate c JOIN job_application a ON a.candidate_id = c.id WHERE a.id = ?""",
                Boolean.class, applicationId));
    }

    private int auditCount(String action, UUID entityId) {
        return jdbc.queryForObject("SELECT count(*) FROM audit_log WHERE action = ? AND entity_id = ?", Integer.class,
                action, entityId);
    }
}
