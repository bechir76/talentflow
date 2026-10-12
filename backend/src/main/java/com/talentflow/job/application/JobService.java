package com.talentflow.job.application;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
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
    private final Clock clock;

    public JobService(JobRepository jobs, DepartmentRepository departments, CurrentUserProvider currentUser,
            Clock clock) {
        this.jobs = jobs;
        this.departments = departments;
        this.currentUser = currentUser;
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
        return new JobStats(counts.get(JobStatus.PUBLISHED), counts.get(JobStatus.DRAFT), counts.get(JobStatus.CLOSED));
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
        return withDepartments(List.of(job)).getFirst();
    }

    private Page<JobView> withDepartments(Page<Job> page) {
        Map<UUID, String> names = departmentNames(page.getContent());
        return page.map(job -> new JobView(job, names.get(job.getDepartmentId())));
    }

    private List<JobView> withDepartments(List<Job> jobList) {
        Map<UUID, String> names = departmentNames(jobList);
        return jobList.stream().map(job -> new JobView(job, names.get(job.getDepartmentId()))).toList();
    }

    private Map<UUID, String> departmentNames(List<Job> jobList) {
        List<UUID> ids = jobList.stream().map(Job::getDepartmentId).distinct().toList();
        return departments.findAllById(ids).stream()
                .collect(Collectors.toMap(Department::getId, Department::getName, (a, b) -> a));
    }
}
