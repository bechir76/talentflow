package com.talentflow.job.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.talentflow.job.domain.Department;
import com.talentflow.job.domain.DepartmentRepository;
import com.talentflow.job.domain.Job;
import com.talentflow.job.domain.JobDetails;
import com.talentflow.job.domain.JobRepository;
import com.talentflow.job.domain.JobStatus;
import com.talentflow.job.domain.WorkMode;
import com.talentflow.shared.application.CurrentUser;
import com.talentflow.shared.application.CurrentUserProvider;
import com.talentflow.shared.domain.BusinessRuleException;
import com.talentflow.shared.domain.NotFoundException;
import com.talentflow.shared.domain.Role;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-01T10:00:00Z");
    private static final UUID DEPARTMENT_ID = UUID.randomUUID();

    @Mock
    private JobRepository jobs;
    @Mock
    private DepartmentRepository departments;
    @Mock
    private CurrentUserProvider currentUser;
    @Mock
    private ApplicantCounter applicants;

    private JobService service;

    @BeforeEach
    void setUp() {
        service = new JobService(jobs, departments, currentUser, applicants, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsJobOwnedByTheCurrentUser() {
        when(departments.existsById(DEPARTMENT_ID)).thenReturn(true);
        when(departments.findAllById(any())).thenReturn(List.of(new Department(DEPARTMENT_ID, "Engineering")));
        when(currentUser.currentUser()).thenReturn(new CurrentUser("kc-1", null, null, Set.of(Role.RECRUITER)));
        when(jobs.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        JobView view = service.create(details(), JobStatus.PUBLISHED);

        assertThat(view.applicantCount()).isZero();
        assertThat(view.job().getCreatedBy()).isEqualTo("kc-1");
        assertThat(view.job().getCreatedAt()).isEqualTo(NOW);
        assertThat(view.departmentName()).isEqualTo("Engineering");
    }

    @Test
    void rejectsUnknownDepartment() {
        when(departments.existsById(DEPARTMENT_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.create(details(), JobStatus.DRAFT))
                .isInstanceOf(BusinessRuleException.class);
        verify(jobs, never()).save(any());
    }

    @Test
    void hidesDraftsFromThePublicCareerPage() {
        Job draft = Job.create(details(), JobStatus.DRAFT, "kc-1", NOW);
        when(jobs.findById(draft.getId())).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.getPublished(draft.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void reportsMissingJobOnUpdate() {
        UUID id = UUID.randomUUID();
        when(departments.existsById(DEPARTMENT_ID)).thenReturn(true);
        when(jobs.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, details()))
                .isInstanceOf(NotFoundException.class);
    }

    private static JobDetails details() {
        return new JobDetails("Java Developer", "Build reliable services. ".repeat(4), List.of("Java"), "Berlin",
                WorkMode.REMOTE, null, null, DEPARTMENT_ID);
    }
}
