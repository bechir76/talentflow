package com.talentflow.pipeline.application;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentflow.application.domain.ApplicationStage;
import com.talentflow.interview.application.InterviewService;
import com.talentflow.interview.application.InterviewService.CalendarEntry;
import com.talentflow.interview.domain.Interview.Interviewer;
import com.talentflow.job.application.JobService;
import com.talentflow.job.application.JobStats;
import com.talentflow.pipeline.application.DashboardReader.JobApplicationCount;
import com.talentflow.pipeline.application.DashboardReader.TopMatch;

/**
 * Key figures and charts for the staff dashboard. Interview figures are personal for hiring managers.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    static final Duration APPLICATIONS_WINDOW = Duration.ofDays(30);
    static final Duration TIME_TO_HIRE_WINDOW = Duration.ofDays(90);
    static final Duration TOP_MATCHES_WINDOW = Duration.ofDays(7);
    static final Duration UPCOMING_WINDOW = Duration.ofDays(7);
    private static final int CHART_JOBS = 6;
    private static final int TOP_MATCHES = 3;
    private static final int UPCOMING_INTERVIEWS = 5;
    private static final List<ApplicationStage> FUNNEL = List.of(ApplicationStage.APPLIED, ApplicationStage.SCREENING,
            ApplicationStage.INTERVIEW, ApplicationStage.OFFER, ApplicationStage.HIRED);

    private final DashboardReader reader;
    private final JobService jobs;
    private final InterviewService interviews;
    private final Clock clock;
    private final ZoneId zone;

    public DashboardService(DashboardReader reader, JobService jobs, InterviewService interviews, Clock clock,
            @Value("${talentflow.time-zone}") String zone) {
        this.reader = reader;
        this.jobs = jobs;
        this.interviews = interviews;
        this.clock = clock;
        this.zone = ZoneId.of(zone);
    }

    public Dashboard dashboard() {
        Instant now = clock.instant();
        JobStats jobStats = jobs.stats();
        Map<ApplicationStage, Long> reached = reader.furthestStageReached();
        List<FunnelStep> funnel = new ArrayList<>();
        for (ApplicationStage stage : FUNNEL) {
            funnel.add(new FunnelStep(stage, reached.getOrDefault(stage, 0L)));
        }
        ZonedDateTime weekStart = now.atZone(zone).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toLocalDate().atStartOfDay(zone);
        long interviewsThisWeek = interviews.calendar(weekStart.toInstant(), weekStart.plusWeeks(1).toInstant()).size();
        List<UpcomingInterview> upcoming = interviews.calendar(now, now.plus(UPCOMING_WINDOW)).stream()
                .limit(UPCOMING_INTERVIEWS).map(UpcomingInterview::from).toList();
        return new Dashboard(jobStats.published(), jobStats.activeApplicants(), interviewsThisWeek,
                reader.averageDaysToHire(now.minus(TIME_TO_HIRE_WINDOW)),
                reader.applicationsPerJob(now.minus(APPLICATIONS_WINDOW), CHART_JOBS), funnel,
                reader.topMatches(now.minus(TOP_MATCHES_WINDOW), TOP_MATCHES), upcoming);
    }

    public record Dashboard(long openJobs, long activeCandidates, long interviewsThisWeek, Double averageDaysToHire,
            List<JobApplicationCount> applicationsPerJob, List<FunnelStep> funnel, List<TopMatch> topMatches,
            List<UpcomingInterview> upcomingInterviews) {
    }

    public record FunnelStep(ApplicationStage stage, long applications) {
    }

    public record UpcomingInterview(UUID interviewId, UUID applicationId, String candidateName, String jobTitle,
            Instant scheduledAt, int durationMinutes, List<String> interviewerNames) {

        static UpcomingInterview from(CalendarEntry entry) {
            var interview = entry.interview();
            return new UpcomingInterview(interview.getId(), interview.getApplicationId(), entry.candidateName(),
                    entry.jobTitle(), interview.getScheduledAt(), interview.getDurationMinutes(),
                    interview.getInterviewers().stream().map(Interviewer::name).toList());
        }
    }
}
