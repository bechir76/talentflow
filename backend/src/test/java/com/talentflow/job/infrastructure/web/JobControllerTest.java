package com.talentflow.job.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.talentflow.job.application.JobService;
import com.talentflow.job.application.JobView;
import com.talentflow.job.domain.Job;
import com.talentflow.job.domain.JobDetails;
import com.talentflow.job.domain.JobStatus;
import com.talentflow.job.domain.WorkMode;
import com.talentflow.shared.domain.BusinessRuleException;
import com.talentflow.shared.infrastructure.security.KeycloakRealmRoleConverter;
import com.talentflow.shared.infrastructure.security.SecurityConfig;

@WebMvcTest(JobController.class)
@Import(SecurityConfig.class)
class JobControllerTest {

    private static final UUID DEPARTMENT_ID = UUID.randomUUID();

    private static final String VALID_BODY = """
            {
              "title": "Senior Java Developer",
              "description": "We are looking for an engineer who builds reliable, well-tested backend services.",
              "requiredSkills": ["Java", "Spring Boot"],
              "location": "Berlin, Germany",
              "workMode": "HYBRID",
              "salaryMin": 75000,
              "salaryMax": 95000,
              "departmentId": "%s",
              "status": "PUBLISHED"
            }
            """.formatted(DEPARTMENT_ID);

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private JobService jobService;

    @Test
    void recruiterCreatesJob() {
        JobView view = sampleView(JobStatus.PUBLISHED);
        when(jobService.create(any(JobDetails.class), eq(JobStatus.PUBLISHED))).thenReturn(view);

        var response = mvc.post().uri("/api/v1/jobs").with(as("RECRUITER"))
                .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY);

        assertThat(response).hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "/api/v1/jobs/" + view.job().getId())
                .bodyJson().extractingPath("$.departmentName").isEqualTo("Engineering");
    }

    @Test
    void rejectsInvalidJobWithFieldErrors() {
        var response = mvc.post().uri("/api/v1/jobs").with(as("RECRUITER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"\", \"description\": \"too short\", \"requiredSkills\": []}");

        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson().extractingPath("$.errors[*].field")
                .asArray().contains("title", "description", "requiredSkills", "location", "workMode", "departmentId");
        verify(jobService, never()).create(any(), any());
    }

    @Test
    void reportsBusinessRuleViolationAsUnprocessable() {
        when(jobService.create(any(), any())).thenThrow(new BusinessRuleException("Department does not exist."));

        var response = mvc.post().uri("/api/v1/jobs").with(as("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY);

        assertThat(response).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson().extractingPath("$.detail").isEqualTo("Department does not exist.");
    }

    @Test
    void candidateCannotManageJobs() {
        assertThat(mvc.get().uri("/api/v1/jobs").with(as("CANDIDATE"))).hasStatus(HttpStatus.FORBIDDEN);
        assertThat(mvc.post().uri("/api/v1/jobs").with(as("HIRING_MANAGER"))
                .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY)).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void listsJobsAsPage() {
        when(jobService.search(any(), any())).thenReturn(
                new PageImpl<>(List.of(sampleView(JobStatus.DRAFT)), PageRequest.of(0, 20), 1));

        var response = mvc.get().uri("/api/v1/jobs?status=DRAFT&q=java").with(as("RECRUITER"));

        assertThat(response).hasStatusOk().bodyJson().isLenientlyEqualTo("""
                {"page": 0, "size": 20, "totalElements": 1, "totalPages": 1,
                 "content": [{"title": "Senior Java Developer", "status": "DRAFT", "departmentName": "Engineering"}]}
                """);
    }

    @Test
    void rejectsUnknownStatusValue() {
        assertThat(mvc.get().uri("/api/v1/jobs?status=ARCHIVED").with(as("RECRUITER")))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    static JobView sampleView(JobStatus status) {
        JobDetails details = new JobDetails("Senior Java Developer", "Build reliable services. ".repeat(4),
                List.of("Java"), "Berlin, Germany", WorkMode.HYBRID, 75000, 95000, DEPARTMENT_ID);
        return new JobView(Job.create(details, status, "kc-1", Instant.parse("2026-10-01T10:00:00Z")),
                "Engineering");
    }

    private static RequestPostProcessor as(String role) {
        return jwt().jwt(token -> token.claim("realm_access", Map.of("roles", List.of(role))))
                .authorities(new KeycloakRealmRoleConverter());
    }
}
