package com.talentflow.interview.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talentflow.application.application.ApplicationAccessGuard;
import com.talentflow.application.domain.JobApplication;
import com.talentflow.application.domain.JobApplicationRepository;
import com.talentflow.candidate.application.CandidateService;
import com.talentflow.candidate.domain.Candidate;
import com.talentflow.interview.domain.Feedback;
import com.talentflow.interview.domain.Feedback.Scores;
import com.talentflow.interview.domain.Interview;
import com.talentflow.interview.domain.Interview.Interviewer;
import com.talentflow.interview.domain.InterviewRepository;
import com.talentflow.interview.domain.Recommendation;
import com.talentflow.job.application.JobService;
import com.talentflow.job.application.JobView;
import com.talentflow.shared.application.CurrentUser;
import com.talentflow.shared.application.CurrentUserProvider;
import com.talentflow.shared.domain.BusinessRuleException;
import com.talentflow.shared.domain.ConflictException;
import com.talentflow.shared.domain.ForbiddenException;
import com.talentflow.shared.domain.NotFoundException;
import com.talentflow.shared.domain.Role;
import com.talentflow.user.application.UserAccount;
import com.talentflow.user.application.UserAdminService;

/**
 * Scheduling interviews, the interview calendar and interviewer feedback.
 */
@Service
@Transactional
public class InterviewService {

    static final Duration MAX_CALENDAR_RANGE = Duration.ofDays(62);

    private final InterviewRepository interviews;
    private final JobApplicationRepository applications;
    private final CandidateService candidates;
    private final JobService jobs;
    private final UserAdminService users;
    private final ApplicationAccessGuard access;
    private final CurrentUserProvider currentUser;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public InterviewService(InterviewRepository interviews, JobApplicationRepository applications,
            CandidateService candidates, JobService jobs, UserAdminService users, ApplicationAccessGuard access,
            CurrentUserProvider currentUser, ApplicationEventPublisher events, Clock clock) {
        this.interviews = interviews;
        this.applications = applications;
        this.candidates = candidates;
        this.jobs = jobs;
        this.users = users;
        this.access = access;
        this.currentUser = currentUser;
        this.events = events;
        this.clock = clock;
    }

    public Interview schedule(UUID applicationId, Instant scheduledAt, int durationMinutes, List<String> interviewerIds,
            String meetingLink) {
        JobApplication application = loadApplication(applicationId);
        if (!application.getStage().isActive()) {
            throw new BusinessRuleException("Interviews can only be scheduled for applications in progress.");
        }
        Map<String, UserAccount> available = users.interviewers().stream()
                .collect(Collectors.toMap(UserAccount::id, Function.identity(), (a, b) -> a));
        List<Interviewer> interviewers = interviewerIds.stream().distinct().map(id -> {
            UserAccount account = available.get(id);
            if (account == null) {
                throw new BusinessRuleException("User " + id + " cannot be invited as an interviewer.");
            }
            return new Interviewer(account.id(), account.fullName());
        }).toList();
        Interview interview = interviews.save(Interview.schedule(applicationId, scheduledAt, durationMinutes,
                interviewers, meetingLink, currentUser.currentUser().id(), clock.instant()));
        events.publishEvent(new InterviewScheduled(interview.getId()));
        return interview;
    }

    public void cancel(UUID interviewId) {
        interviews.delete(load(interviewId));
    }

    @Transactional(readOnly = true)
    public List<InterviewWithFeedback> forApplication(UUID applicationId) {
        access.requireAccess(applicationId);
        List<Interview> list = interviews.findByApplicationId(applicationId);
        Map<UUID, List<Feedback>> feedback = interviews.findFeedback(list.stream().map(Interview::getId).toList())
                .stream().collect(Collectors.groupingBy(Feedback::getInterviewId));
        return list.stream()
                .map(interview -> new InterviewWithFeedback(interview, feedback.getOrDefault(interview.getId(), List.of())))
                .toList();
    }

    /** Recruiters and admins see all interviews; hiring managers only their own. */
    @Transactional(readOnly = true)
    public List<CalendarEntry> calendar(Instant from, Instant to) {
        if (!to.isAfter(from) || Duration.between(from, to).compareTo(MAX_CALENDAR_RANGE) > 0) {
            throw new BusinessRuleException("The calendar range must be positive and at most 62 days.");
        }
        CurrentUser user = currentUser.currentUser();
        boolean seesAll = user.hasRole(Role.RECRUITER) || user.hasRole(Role.ADMIN);
        List<Interview> list = interviews.findBetween(from, to, seesAll ? null : user.id());
        return toCalendarEntries(list);
    }

    public Feedback submitFeedback(UUID interviewId, Scores scores, String comment, Recommendation recommendation) {
        Interview interview = load(interviewId);
        CurrentUser user = currentUser.currentUser();
        if (!interview.hasInterviewer(user.id())) {
            throw new ForbiddenException("Only interviewers of this interview can give feedback.");
        }
        if (interviews.hasFeedback(interviewId, user.id())) {
            throw new ConflictException("You have already submitted feedback for this interview.");
        }
        return interviews.save(Feedback.submit(interviewId, user.id(), user.fullName(), scores, comment,
                recommendation, clock.instant()));
    }

    /** For the candidate's data export. */
    @Transactional(readOnly = true)
    public List<Interview> interviewsOf(UUID applicationId) {
        return interviews.findByApplicationId(applicationId);
    }

    /** Part of a candidate's erasure. */
    public void anonymizeForApplication(UUID applicationId) {
        List<Interview> list = interviews.findByApplicationId(applicationId);
        list.forEach(Interview::anonymize);
        interviews.findFeedback(list.stream().map(Interview::getId).toList()).forEach(Feedback::anonymize);
    }

    private List<CalendarEntry> toCalendarEntries(List<Interview> list) {
        List<UUID> applicationIds = list.stream().map(Interview::getApplicationId).distinct().toList();
        Map<UUID, JobApplication> byId = applicationIds.stream()
                .flatMap(id -> applications.findById(id).stream())
                .collect(Collectors.toMap(JobApplication::getId, Function.identity()));
        Map<UUID, Candidate> people = byId.isEmpty() ? Map.of()
                : candidates.findAllById(byId.values().stream().map(JobApplication::getCandidateId).distinct().toList());
        List<UUID> jobIds = byId.values().stream().map(JobApplication::getJobId).distinct().toList();
        Map<UUID, JobView> jobViews = jobIds.isEmpty() ? Map.of() : jobs.findViews(jobIds);
        return list.stream().map(interview -> {
            JobApplication application = byId.get(interview.getApplicationId());
            Candidate candidate = application == null ? null : people.get(application.getCandidateId());
            JobView job = application == null ? null : jobViews.get(application.getJobId());
            return new CalendarEntry(interview, candidate == null ? null : candidate.getFullName(),
                    job == null ? null : job.job().getTitle());
        }).toList();
    }

    private JobApplication loadApplication(UUID applicationId) {
        return applications.findById(applicationId).orElseThrow(() -> new NotFoundException("Application", applicationId));
    }

    private Interview load(UUID interviewId) {
        return interviews.findById(interviewId).orElseThrow(() -> new NotFoundException("Interview", interviewId));
    }

    public record InterviewWithFeedback(Interview interview, List<Feedback> feedback) {
    }

    public record CalendarEntry(Interview interview, String candidateName, String jobTitle) {
    }
}
