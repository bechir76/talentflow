package com.talentflow.gdpr.infrastructure.persistence;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.talentflow.gdpr.domain.RetentionPolicy;
import com.talentflow.gdpr.domain.RetentionPolicyRepository;

@Repository
class JpaRetentionPolicyRepository implements RetentionPolicyRepository {

    private final SpringDataRetentionPolicyRepository jpa;
    private final JdbcTemplate jdbc;

    JpaRetentionPolicyRepository(SpringDataRetentionPolicyRepository jpa, JdbcTemplate jdbc) {
        this.jpa = jpa;
        this.jdbc = jdbc;
    }

    @Override
    public RetentionPolicy get() {
        return jpa.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("The retention policy row is missing"));
    }

    @Override
    public List<UUID> findExpiredRejectedCandidates(Instant rejectedBefore) {
        return jdbc.queryForList("""
                SELECT c.id FROM candidate c
                JOIN job_application a ON a.candidate_id = c.id
                WHERE NOT c.anonymized
                GROUP BY c.id
                HAVING bool_and(a.stage = 'REJECTED') AND max(a.stage_changed_at) < ?""",
                UUID.class, Timestamp.from(rejectedBefore));
    }
}
