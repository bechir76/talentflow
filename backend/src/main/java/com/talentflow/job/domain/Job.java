package com.talentflow.job.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.talentflow.shared.domain.BusinessRuleException;

@Entity
@Table(name = "job")
public class Job {

    public static final String CURRENCY = "EUR";

    @Id
    private UUID id;

    @Version
    private long version;

    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "required_skills", columnDefinition = "text[]")
    private List<String> requiredSkills = new ArrayList<>();

    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode")
    private WorkMode workMode;

    @Column(name = "salary_min")
    private Integer salaryMin;

    @Column(name = "salary_max")
    private Integer salaryMax;

    private String currency;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Job() {
    }

    public static Job create(JobDetails details, JobStatus initialStatus, String createdBy, Instant now) {
        if (initialStatus == JobStatus.CLOSED) {
            throw new BusinessRuleException("A new job must be a draft or published.");
        }
        Job job = new Job();
        job.id = UUID.randomUUID();
        job.currency = CURRENCY;
        job.status = initialStatus;
        job.createdBy = createdBy;
        job.createdAt = now;
        job.apply(details, now);
        return job;
    }

    public void update(JobDetails details, Instant now) {
        apply(details, now);
    }

    public void changeStatus(JobStatus target, Instant now) {
        if (status == target) {
            return;
        }
        if (!status.canChangeTo(target)) {
            throw new BusinessRuleException("A job cannot change from " + status + " to " + target + ".");
        }
        status = target;
        updatedAt = now;
    }

    public boolean isPublished() {
        return status == JobStatus.PUBLISHED;
    }

    private void apply(JobDetails details, Instant now) {
        title = details.title();
        description = details.description();
        requiredSkills = new ArrayList<>(details.requiredSkills());
        location = details.location();
        workMode = details.workMode();
        salaryMin = details.salaryMin();
        salaryMax = details.salaryMax();
        departmentId = details.departmentId();
        updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getRequiredSkills() {
        return List.copyOf(requiredSkills);
    }

    public String getLocation() {
        return location;
    }

    public WorkMode getWorkMode() {
        return workMode;
    }

    public Integer getSalaryMin() {
        return salaryMin;
    }

    public Integer getSalaryMax() {
        return salaryMax;
    }

    public String getCurrency() {
        return currency;
    }

    public JobStatus getStatus() {
        return status;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
