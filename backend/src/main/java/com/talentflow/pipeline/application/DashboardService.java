package com.talentflow.pipeline.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentflow.application.domain.ApplicationStage;
import com.talentflow.job.application.JobService;
import com.talentflow.job.application.JobStats;
import com.talentflow.pipeline.application.DashboardReader.JobApplicationCount;
import com.talentflow.pipeline.application.DashboardReader.TopMatch;

/**
 * Key figures and charts for the recruiter dashboard.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    static final Duration APPLICATIONS_WINDOW = Duration.ofDays(30);
    static final Duration TIME_TO_HIRE_WINDOW = Duration.ofDays(90);
    static final Duration TOP_MATCHES_WINDOW = Duration.ofDays(7);
    private static final int CHART_JOBS = 6;
    private static final int TOP_MATCHES = 3;
    private static final List<ApplicationStage> FUNNEL = List.of(ApplicationStage.APPLIED, ApplicationStage.SCREENING,
            ApplicationStage.INTERVIEW, ApplicationStage.OFFER, ApplicationStage.HIRED);

    private final DashboardReader reader;
    private final JobService jobs;
    private final Clock clock;

    public DashboardService(DashboardReader reader, JobService jobs, Clock clock) {
        this.reader = reader;
        this.jobs = jobs;
        this.clock = clock;
    }

    public Dashboard dashboard() {
        Instant now = clock.instant();
        JobStats jobStats = jobs.stats();
        Map<ApplicationStage, Long> reached = reader.furthestStageReached();
        List<FunnelStep> funnel = new ArrayList<>();
        for (ApplicationStage stage : FUNNEL) {
            funnel.add(new FunnelStep(stage, reached.getOrDefault(stage, 0L)));
        }
        return new Dashboard(jobStats.published(), jobStats.activeApplicants(),
                reader.averageDaysToHire(now.minus(TIME_TO_HIRE_WINDOW)),
                reader.applicationsPerJob(now.minus(APPLICATIONS_WINDOW), CHART_JOBS), funnel,
                reader.topMatches(now.minus(TOP_MATCHES_WINDOW), TOP_MATCHES));
    }

    public record Dashboard(long openJobs, long activeCandidates, Double averageDaysToHire,
            List<JobApplicationCount> applicationsPerJob, List<FunnelStep> funnel, List<TopMatch> topMatches) {
    }

    public record FunnelStep(ApplicationStage stage, long applications) {
    }
}
