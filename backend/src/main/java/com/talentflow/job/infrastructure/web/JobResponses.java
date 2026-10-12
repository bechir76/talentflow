package com.talentflow.job.infrastructure.web;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.talentflow.job.application.JobStats;
import com.talentflow.job.application.JobView;
import com.talentflow.job.domain.Department;
import com.talentflow.job.domain.Job;
import com.talentflow.job.domain.JobStatus;
import com.talentflow.job.domain.WorkMode;

/**
 * Response DTOs for the job endpoints. Entities never leave the application layer.
 */
final class JobResponses {

    private JobResponses() {
    }

    /** Full job as seen by staff. */
    record JobResponse(UUID id, String title, String description, List<String> requiredSkills, String location,
            WorkMode workMode, Integer salaryMin, Integer salaryMax, String currency, JobStatus status,
            UUID departmentId, String departmentName, Instant createdAt, Instant updatedAt) {

        static JobResponse from(JobView view) {
            Job job = view.job();
            return new JobResponse(job.getId(), job.getTitle(), job.getDescription(), job.getRequiredSkills(),
                    job.getLocation(), job.getWorkMode(), job.getSalaryMin(), job.getSalaryMax(), job.getCurrency(),
                    job.getStatus(), job.getDepartmentId(), view.departmentName(), job.getCreatedAt(),
                    job.getUpdatedAt());
        }
    }

    /** Row in the staff jobs table. */
    record JobSummaryResponse(UUID id, String title, String departmentName, String location, WorkMode workMode,
            JobStatus status, long applicantCount, Instant createdAt) {

        static JobSummaryResponse from(JobView view) {
            Job job = view.job();
            return new JobSummaryResponse(job.getId(), job.getTitle(), view.departmentName(), job.getLocation(),
                    job.getWorkMode(), job.getStatus(), view.applicantCount(), job.getCreatedAt());
        }
    }

    /** Published job on the career page. Contains no internal fields. */
    record PublicJobResponse(UUID id, String title, String description, List<String> requiredSkills,
            String location, WorkMode workMode, Integer salaryMin, Integer salaryMax, String currency,
            String departmentName, Instant postedAt) {

        static PublicJobResponse from(JobView view) {
            Job job = view.job();
            return new PublicJobResponse(job.getId(), job.getTitle(), job.getDescription(), job.getRequiredSkills(),
                    job.getLocation(), job.getWorkMode(), job.getSalaryMin(), job.getSalaryMax(), job.getCurrency(),
                    view.departmentName(), job.getCreatedAt());
        }
    }

    /** Card in the career page list; the description is left out to keep the list light. */
    record PublicJobSummaryResponse(UUID id, String title, List<String> requiredSkills, String location,
            WorkMode workMode, Integer salaryMin, Integer salaryMax, String currency, String departmentName,
            Instant postedAt) {

        static PublicJobSummaryResponse from(JobView view) {
            Job job = view.job();
            return new PublicJobSummaryResponse(job.getId(), job.getTitle(), job.getRequiredSkills(),
                    job.getLocation(), job.getWorkMode(), job.getSalaryMin(), job.getSalaryMax(), job.getCurrency(),
                    view.departmentName(), job.getCreatedAt());
        }
    }

    record JobStatsResponse(long published, long drafts, long closed, long activeApplicants) {

        static JobStatsResponse from(JobStats stats) {
            return new JobStatsResponse(stats.published(), stats.drafts(), stats.closed(), stats.activeApplicants());
        }
    }

    record DepartmentResponse(UUID id, String name) {

        static DepartmentResponse from(Department department) {
            return new DepartmentResponse(department.getId(), department.getName());
        }
    }
}
