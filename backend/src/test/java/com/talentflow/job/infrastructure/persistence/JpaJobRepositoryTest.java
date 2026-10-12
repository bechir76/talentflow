package com.talentflow.job.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.talentflow.TestcontainersConfiguration;
import com.talentflow.job.domain.DepartmentRepository;
import com.talentflow.job.domain.Job;
import com.talentflow.job.domain.JobDetails;
import com.talentflow.job.domain.JobRepository;
import com.talentflow.job.domain.JobSearchCriteria;
import com.talentflow.job.domain.JobStatus;
import com.talentflow.job.domain.WorkMode;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JpaJobRepository.class, JpaDepartmentRepository.class})
class JpaJobRepositoryTest {

    private static final Instant NOW = Instant.parse("2026-10-01T10:00:00Z");

    @Autowired
    private JobRepository jobs;

    @Autowired
    private DepartmentRepository departments;

    private UUID engineering;

    @BeforeEach
    void setUp() {
        engineering = departments.findAllOrderedByName().stream()
                .filter(department -> department.getName().equals("Engineering"))
                .findFirst().orElseThrow().getId();
    }

    @Test
    void seedsDepartments() {
        assertThat(departments.findAllOrderedByName()).extracting("name")
                .contains("Engineering", "Product & Design", "Sales");
    }

    @Test
    void storesSkillsAsArrayAndSearchesByTextAndStatus() {
        jobs.save(job("Senior Java Developer", "Berlin", JobStatus.PUBLISHED, List.of("Java", "Spring Boot")));
        jobs.save(job("Product Designer", "Munich", JobStatus.DRAFT, List.of("Figma")));
        jobs.save(job("Java 50% Remote", "Hamburg", JobStatus.PUBLISHED, List.of("Java")));

        var page = jobs.search(new JobSearchCriteria("java", JobStatus.PUBLISHED, null, null),
                PageRequest.of(0, 10, Sort.by("title")));

        assertThat(page.getContent()).extracting(Job::getTitle)
                .containsExactly("Java 50% Remote", "Senior Java Developer");
        assertThat(page.getContent().get(1).getRequiredSkills()).containsExactly("Java", "Spring Boot");
    }

    @Test
    void treatsLikeWildcardsInSearchTextLiterally() {
        jobs.save(job("Java 50% Remote", "Hamburg", JobStatus.PUBLISHED, List.of("Java")));
        jobs.save(job("Data Analyst", "Cologne", JobStatus.PUBLISHED, List.of("SQL")));

        var page = jobs.search(new JobSearchCriteria("%", null, null, null), PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Job::getTitle).containsExactly("Java 50% Remote");
    }

    @Test
    void searchesByLocationAndWorkMode() {
        jobs.save(job("Backend Engineer", "Berlin, Germany", JobStatus.PUBLISHED, List.of("Go")));
        jobs.save(job("Frontend Engineer", "Munich", JobStatus.PUBLISHED, List.of("React")));

        var page = jobs.search(JobSearchCriteria.publishedOnly("berlin", WorkMode.REMOTE), PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Job::getTitle).containsExactly("Backend Engineer");
    }

    @Test
    void countsJobsPerStatusIncludingEmptyStatuses() {
        jobs.save(job("A", "Berlin", JobStatus.PUBLISHED, List.of("Java")));
        jobs.save(job("B", "Berlin", JobStatus.PUBLISHED, List.of("Java")));
        jobs.save(job("C", "Berlin", JobStatus.DRAFT, List.of("Java")));

        assertThat(jobs.countByStatus())
                .containsEntry(JobStatus.PUBLISHED, 2L)
                .containsEntry(JobStatus.DRAFT, 1L)
                .containsEntry(JobStatus.CLOSED, 0L);
    }

    private Job job(String title, String location, JobStatus status, List<String> skills) {
        JobDetails details = new JobDetails(title, "Description of the role. ".repeat(4), skills, location,
                WorkMode.REMOTE, null, null, engineering);
        return Job.create(details, status, "kc-1", NOW);
    }
}
