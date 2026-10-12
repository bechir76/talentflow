package com.talentflow.job.infrastructure.persistence;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.talentflow.job.domain.Job;
import com.talentflow.job.domain.JobSearchCriteria;

final class JobSpecifications {

    private static final char LIKE_ESCAPE = '\\';

    private JobSpecifications() {
    }

    static Specification<Job> matching(JobSearchCriteria criteria) {
        Specification<Job> spec = Specification.unrestricted();
        if (criteria.status() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), criteria.status()));
        }
        if (criteria.departmentId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("departmentId"), criteria.departmentId()));
        }
        if (criteria.workMode() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("workMode"), criteria.workMode()));
        }
        if (criteria.text() != null && !criteria.text().isBlank()) {
            String pattern = "%" + escapeLike(criteria.text().strip().toLowerCase(Locale.ROOT)) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), pattern, LIKE_ESCAPE),
                    cb.like(cb.lower(root.get("location")), pattern, LIKE_ESCAPE)));
        }
        return spec;
    }

    private static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
