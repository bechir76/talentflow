package com.talentflow.job.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.talentflow.job.application.JobService;
import com.talentflow.job.application.JobView;
import com.talentflow.job.domain.JobStatus;
import com.talentflow.shared.domain.NotFoundException;
import com.talentflow.shared.infrastructure.security.SecurityConfig;

@WebMvcTest(PublicJobController.class)
@Import(SecurityConfig.class)
class PublicJobControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private JobService jobService;

    @Test
    void listsPublishedJobsWithoutAuthentication() {
        JobView view = JobControllerTest.sampleView(JobStatus.PUBLISHED);
        when(jobService.searchPublished(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(view), PageRequest.of(0, 12), 1));

        var response = mvc.get().uri("/api/v1/public/jobs?workMode=HYBRID");

        assertThat(response).hasStatusOk().bodyJson()
                .extractingPath("$.content[0].title").isEqualTo("Senior Java Developer");
    }

    @Test
    void doesNotExposeInternalFields() {
        JobView view = JobControllerTest.sampleView(JobStatus.PUBLISHED);
        when(jobService.getPublished(view.job().getId())).thenReturn(view);

        var response = mvc.get().uri("/api/v1/public/jobs/" + view.job().getId());

        assertThat(response).hasStatusOk().bodyText()
                .doesNotContain("createdBy", "kc-1", "status", "departmentId");
    }

    @Test
    void returnsNotFoundForUnpublishedJob() {
        UUID id = UUID.randomUUID();
        when(jobService.getPublished(id)).thenThrow(new NotFoundException("Job", id));

        assertThat(mvc.get().uri("/api/v1/public/jobs/" + id)).hasStatus(HttpStatus.NOT_FOUND);
    }
}
