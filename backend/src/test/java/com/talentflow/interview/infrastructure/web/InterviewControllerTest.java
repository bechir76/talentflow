package com.talentflow.interview.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.talentflow.interview.application.InterviewService;
import com.talentflow.shared.application.CurrentUserProvider;
import com.talentflow.shared.infrastructure.security.KeycloakRealmRoleConverter;
import com.talentflow.shared.infrastructure.security.SecurityConfig;

@WebMvcTest(InterviewController.class)
@Import(SecurityConfig.class)
class InterviewControllerTest {

    private static final UUID ID = UUID.randomUUID();

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private InterviewService interviews;
    @MockitoBean
    private CurrentUserProvider currentUser;

    @Test
    void hiringManagersCannotSchedule() {
        var response = mvc.post().uri("/api/v1/applications/{id}/interviews", ID).with(as("HIRING_MANAGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"scheduledAt\": \"2099-01-01T10:00:00Z\", \"durationMinutes\": 60, \"interviewerIds\": [\"x\"]}");

        assertThat(response).hasStatus(HttpStatus.FORBIDDEN);
        verify(interviews, never()).schedule(any(), any(), anyInt(), any(), any());
    }

    @Test
    void validatesScheduleRequest() {
        var response = mvc.post().uri("/api/v1/applications/{id}/interviews", ID).with(as("RECRUITER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"scheduledAt\": \"2001-01-01T10:00:00Z\", \"durationMinutes\": 5, \"interviewerIds\": []}");

        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST).bodyJson().extractingPath("$.errors[*].field")
                .asArray().contains("scheduledAt", "durationMinutes", "interviewerIds");
    }

    @Test
    void validatesScorecardRange() {
        var response = mvc.post().uri("/api/v1/interviews/{id}/feedback", ID).with(as("HIRING_MANAGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"technical\": 6, \"communication\": 3, \"cultureFit\": 0, \"recommendation\": \"HIRE\"}");

        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void candidatesCannotSeeTheCalendar() {
        assertThat(mvc.get().uri("/api/v1/interviews?from=2026-10-01T00:00:00Z&to=2026-10-08T00:00:00Z").with(as("CANDIDATE")))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    private static RequestPostProcessor as(String role) {
        return jwt().jwt(token -> token.claim("realm_access", Map.of("roles", List.of(role))))
                .authorities(new KeycloakRealmRoleConverter());
    }
}
