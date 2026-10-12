package com.talentflow.application.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.talentflow.application.TestFiles;
import com.talentflow.application.application.ApplyForJobService;
import com.talentflow.application.domain.JobApplication;
import com.talentflow.shared.infrastructure.security.KeycloakRealmRoleConverter;
import com.talentflow.shared.infrastructure.security.SecurityConfig;

@WebMvcTest(ApplicationController.class)
@Import(SecurityConfig.class)
class ApplicationControllerTest {

    private static final UUID JOB_ID = UUID.randomUUID();
    private static final MockMultipartFile PDF =
            new MockMultipartFile("cv", "cv.pdf", "application/pdf", TestFiles.MINIMAL_PDF);

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private ApplyForJobService applyForJob;

    @Test
    void candidateAppliesWithCvAndConsent() {
        JobApplication application = JobApplication.submit(JOB_ID, UUID.randomUUID(), "key.pdf", null,
                Instant.parse("2026-10-01T10:00:00Z"));
        when(applyForJob.apply(eq(JOB_ID), any())).thenReturn(application);

        var response = mvc.post().uri("/api/v1/jobs/{id}/applications", JOB_ID).multipart()
                .file(PDF).param("consent", "true").param("phone", "+49 30 123").with(as("CANDIDATE"));

        assertThat(response).hasStatus(HttpStatus.CREATED)
                .bodyJson().extractingPath("$.stage").isEqualTo("APPLIED");
        verify(applyForJob).apply(eq(JOB_ID), argThat(command -> command.consentGiven()
                && "+49 30 123".equals(command.phone()) && command.cv().size() == TestFiles.MINIMAL_PDF.length));
    }

    @Test
    void rejectsNonPdfUploadAsUnprocessable() {
        var notPdf = new MockMultipartFile("cv", "cv.pdf", "application/pdf", "hello".getBytes());

        var response = mvc.post().uri("/api/v1/jobs/{id}/applications", JOB_ID).multipart()
                .file(notPdf).param("consent", "true").with(as("CANDIDATE"));

        assertThat(response).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        verify(applyForJob, never()).apply(any(), any());
    }

    @Test
    void requiresTheCvPart() {
        var response = mvc.post().uri("/api/v1/jobs/{id}/applications", JOB_ID).multipart()
                .param("consent", "true").with(as("CANDIDATE"));

        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void staffCannotApply() {
        var response = mvc.post().uri("/api/v1/jobs/{id}/applications", JOB_ID).multipart()
                .file(PDF).param("consent", "true").with(as("RECRUITER"));

        assertThat(response).hasStatus(HttpStatus.FORBIDDEN);
    }

    private static RequestPostProcessor as(String role) {
        return jwt().jwt(token -> token.claim("realm_access", Map.of("roles", List.of(role))))
                .authorities(new KeycloakRealmRoleConverter());
    }
}
