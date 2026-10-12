package com.talentflow.job.application;

import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentflow.job.domain.Department;
import com.talentflow.job.domain.DepartmentRepository;
import com.talentflow.job.domain.Job;
import com.talentflow.job.domain.JobDetails;
import com.talentflow.job.domain.JobRepository;
import com.talentflow.job.domain.JobSearchCriteria;
import com.talentflow.job.domain.JobStatus;
import com.talentflow.job.domain.WorkMode;
import com.talentflow.shared.application.CurrentUserProvider;
import com.talentflow.shared.domain.BusinessRuleException;
import com.talentflow.shared.domain.NotFoundException;

/**
 * Use cases for managing job postings and showing them on the career page.
 */
@Service
@Transactional
public class JobService {

    private final JobRepository jobs;
    private final DepartmentRepository departments;
    private final CurrentUserProvider currentUser;
    private final ApplicantCounter applicants;
    private final Clock clock;

    public JobService(JobRepository jobs, DepartmentRepository departments, CurrentUserProvider currentUser,
            ApplicantCounter applicants, Clock clock) {
        this.jobs = jobs;
        this.departments = departments;
        this.currentUser = currentUser;
        this.applicants = applicants;
        this.clock = clock;
    }

    public JobView create(JobDetails details, JobStatus initialStatus) {
        requireDepartment(details.departmentId());
        Job job = Job.create(details, initialStatus, currentUser.currentUser().id(), clock.instant());
        return withDepartment(jobs.save(job));
    }

    public JobView update(UUID id, JobDetails details) {
        requireDepartment(details.departmentId());
        Job job = load(id);
        job.update(details, clock.instant());
        return withDepartment(job);
    }

    public JobView changeStatus(UUID id, JobStatus status) {
        Job job = load(id);
        job.changeStatus(status, clock.instant());
        return withDepartment(job);
    }

    @Transactional(readOnly = true)
    public JobView get(UUID id) {
        return withDepartment(load(id));
    }

    @Transactional(readOnly = true)
    public Page<JobView> search(JobSearchCriteria criteria, Pageable pageable) {
        return withDepartments(jobs.search(criteria, pageable));
    }

    @Transactional(readOnly = true)
    public JobStats stats() {
        Map<JobStatus, Long> counts = jobs.countByStatus();
        return new JobStats(counts.get(JobStatus.PUBLISHED), counts.get(JobStatus.DRAFT), counts.get(JobStatus.CLOSED),
                applicants.countActive());
    }

    @Transactional(readOnly = true)
    public Page<JobView> searchPublished(String text, WorkMode workMode, Pageable pageable) {
        return withDepartments(jobs.search(JobSearchCriteria.publishedOnly(text, workMode), pageable));
    }

    /** Drafts and closed jobs are reported as missing so their existence is not revealed publicly. */
    @Transactional(readOnly = true)
    public JobView getPublished(UUID id) {
        Job job = jobs.findById(id).filter(Job::isPublished).orElseThrow(() -> new NotFoundException("Job", id));
        return withDepartment(job);
    }

    /** Jobs by id, in any status, for other modules (for example a candidate's application list). */
    @Transactional(readOnly = true)
    public Map<UUID, JobView> findViews(Collection<UUID> ids) {
        return views(jobs.findAllById(ids)).stream()
                .collect(Collectors.toMap(view -> view.job().getId(), Function.identity()));
    }

    @Transactional(readOnly = true)
    public List<Department> departments() {
        return departments.findAllOrderedByName();
    }

    private Job load(UUID id) {
        return jobs.findById(id).orElseThrow(() -> new NotFoundException("Job", id));
    }

    private void requireDepartment(UUID departmentId) {
        if (!departments.existsById(departmentId)) {
            throw new BusinessRuleException("Department " + departmentId + " does not exist.");
        }
    }

    private JobView withDepartment(Job job) {
        return views(List.of(job)).getFirst();
    }

    private Page<JobView> withDepartments(Page<Job> page) {
        List<JobView> content = views(page.getContent());
        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

    /** Adds department names and applicant counts with one query each, whatever the number of jobs. */
    private List<JobView> views(List<Job> jobList) {
        List<UUID> jobIds = jobList.stream().map(Job::getId).toList();
        List<UUID> departmentIds = jobList.stream().map(Job::getDepartmentId).distinct().toList();
        Map<UUID, String> names = departments.findAllById(departmentIds).stream()
                .collect(Collectors.toMap(Department::getId, Department::getName, (a, b) -> a));
        Map<UUID, Long> counts = jobIds.isEmpty() ? Map.of() : applicants.countByJobIds(jobIds);
        return jobList.stream()
                .map(job -> new JobView(job, names.get(job.getDepartmentId()), counts.getOrDefault(job.getId(), 0L)))
                .toList();
    }
}
