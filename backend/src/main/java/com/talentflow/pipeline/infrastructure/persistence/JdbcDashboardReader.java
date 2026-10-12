package com.talentflow.pipeline.infrastructure.persistence;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.talentflow.application.domain.ApplicationStage;
import com.talentflow.pipeline.application.DashboardReader;

/**
 * Reporting queries in plain SQL; they span several modules' tables, so they live outside the entities.
 */
@Repository
class JdbcDashboardReader implements DashboardReader {

    private static final String STAGE_ORDER = """
            CASE %s WHEN 'APPLIED' THEN 0 WHEN 'SCREENING' THEN 1 WHEN 'INTERVIEW' THEN 2
                    WHEN 'OFFER' THEN 3 WHEN 'HIRED' THEN 4 ELSE 0 END""";

    private static final List<ApplicationStage> ORDERED =
            List.of(ApplicationStage.APPLIED, ApplicationStage.SCREENING, ApplicationStage.INTERVIEW,
                    ApplicationStage.OFFER, ApplicationStage.HIRED);

    private final JdbcTemplate jdbc;

    JdbcDashboardReader(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<JobApplicationCount> applicationsPerJob(Instant since, int limit) {
        return jdbc.query("""
                SELECT j.id, j.title, count(a.id) AS total
                FROM job_application a JOIN job j ON j.id = a.job_id
                WHERE a.applied_at >= ?
                GROUP BY j.id, j.title
                ORDER BY total DESC, j.title
                LIMIT ?""",
                (rs, row) -> new JobApplicationCount(rs.getObject("id", UUID.class), rs.getString("title"),
                        rs.getLong("total")),
                Timestamp.from(since), limit);
    }

    @Override
    public Map<ApplicationStage, Long> furthestStageReached() {
        // An application reached a stage if it is there now or its history ever moved it there.
        long[] perOrdinal = new long[ORDERED.size()];
        jdbc.query("""
                SELECT reached, count(*) AS total FROM (
                    SELECT GREATEST(%s, COALESCE(max(%s), 0)) AS reached
                    FROM job_application a LEFT JOIN stage_change sc ON sc.application_id = a.id
                    GROUP BY a.id, a.stage
                ) furthest GROUP BY reached""".formatted(STAGE_ORDER.formatted("a.stage"),
                        STAGE_ORDER.formatted("sc.to_stage")),
                rs -> {
                    perOrdinal[rs.getInt("reached")] = rs.getLong("total");
                });
        Map<ApplicationStage, Long> reached = new EnumMap<>(ApplicationStage.class);
        long cumulative = 0;
        for (int ordinal = ORDERED.size() - 1; ordinal >= 0; ordinal--) {
            cumulative += perOrdinal[ordinal];
            reached.put(ORDERED.get(ordinal), cumulative);
        }
        return reached;
    }

    @Override
    public Double averageDaysToHire(Instant since) {
        return jdbc.queryForObject("""
                SELECT avg(extract(epoch FROM (sc.changed_at - a.applied_at)) / 86400)
                FROM stage_change sc JOIN job_application a ON a.id = sc.application_id
                WHERE sc.to_stage = 'HIRED' AND sc.changed_at >= ?""", Double.class, Timestamp.from(since));
    }

    @Override
    public List<TopMatch> topMatches(Instant since, int limit) {
        return jdbc.query("""
                SELECT a.id, c.full_name, j.title, a.ai_score
                FROM job_application a
                JOIN candidate c ON c.id = a.candidate_id
                JOIN job j ON j.id = a.job_id
                WHERE a.ai_status = 'DONE' AND a.stage NOT IN ('HIRED', 'REJECTED')
                  AND a.applied_at >= ? AND NOT c.anonymized
                ORDER BY a.ai_score DESC, a.applied_at DESC
                LIMIT ?""",
                (rs, row) -> new TopMatch(rs.getObject("id", UUID.class), rs.getString("full_name"),
                        rs.getString("title"), rs.getInt("ai_score")),
                Timestamp.from(since), limit);
    }
}
