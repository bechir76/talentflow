package com.talentflow.job.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.talentflow.shared.domain.BusinessRuleException;

class JobTest {

    private static final Instant NOW = Instant.parse("2026-10-01T10:00:00Z");
    private static final Instant LATER = NOW.plusSeconds(60);

    static JobDetails details(List<String> skills, Integer salaryMin, Integer salaryMax) {
        return new JobDetails("  Senior Java Developer ", "Build reliable services. ".repeat(4), skills,
                "Berlin, Germany", WorkMode.HYBRID, salaryMin, salaryMax, UUID.randomUUID());
    }

    @Test
    void createsJobWithTrimmedFieldsAndEuroCurrency() {
        Job job = Job.create(details(List.of("Java"), 70000, 90000), JobStatus.DRAFT, "user-1", NOW);

        assertThat(job.getId()).isNotNull();
        assertThat(job.getTitle()).isEqualTo("Senior Java Developer");
        assertThat(job.getCurrency()).isEqualTo("EUR");
        assertThat(job.getStatus()).isEqualTo(JobStatus.DRAFT);
        assertThat(job.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void normalizesSkillsByTrimmingAndRemovingDuplicates() {
        JobDetails details = details(List.of(" Java ", "java", "", "Spring Boot", "JAVA"), null, null);

        assertThat(details.requiredSkills()).containsExactly("Java", "Spring Boot");
    }

    @Test
    void rejectsJobWithoutSkills() {
        assertThatThrownBy(() -> details(List.of(" "), null, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("at least one required skill");
    }

    @Test
    void rejectsSalaryRangeWhereMinimumExceedsMaximum() {
        assertThatThrownBy(() -> details(List.of("Java"), 90000, 70000))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("minimum salary");
    }

    @Test
    void cannotBeCreatedClosed() {
        assertThatThrownBy(() -> Job.create(details(List.of("Java"), null, null), JobStatus.CLOSED, "user-1", NOW))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void followsTheStatusLifecycle() {
        Job job = Job.create(details(List.of("Java"), null, null), JobStatus.DRAFT, "user-1", NOW);

        job.changeStatus(JobStatus.PUBLISHED, LATER);
        assertThat(job.isPublished()).isTrue();
        assertThat(job.getUpdatedAt()).isEqualTo(LATER);

        job.changeStatus(JobStatus.CLOSED, LATER);
        job.changeStatus(JobStatus.PUBLISHED, LATER);
        assertThat(job.getStatus()).isEqualTo(JobStatus.PUBLISHED);
    }

    @Test
    void rejectsGoingBackToDraft() {
        Job job = Job.create(details(List.of("Java"), null, null), JobStatus.PUBLISHED, "user-1", NOW);

        assertThatThrownBy(() -> job.changeStatus(JobStatus.DRAFT, LATER))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("PUBLISHED to DRAFT");
    }
}
