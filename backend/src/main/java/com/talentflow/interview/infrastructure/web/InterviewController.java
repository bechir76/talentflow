package com.talentflow.interview.infrastructure.web;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentflow.interview.application.InterviewService;
import com.talentflow.interview.application.InterviewService.CalendarEntry;
import com.talentflow.interview.application.InterviewService.InterviewWithFeedback;
import com.talentflow.interview.domain.Feedback;
import com.talentflow.interview.domain.Feedback.Scores;
import com.talentflow.interview.domain.Interview;
import com.talentflow.interview.domain.Interview.Interviewer;
import com.talentflow.interview.domain.Recommendation;
import com.talentflow.shared.application.CurrentUserProvider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Interviews")
public class InterviewController {

    private final InterviewService interviews;
    private final CurrentUserProvider currentUser;

    public InterviewController(InterviewService interviews, CurrentUserProvider currentUser) {
        this.interviews = interviews;
        this.currentUser = currentUser;
    }

    @PostMapping("/api/v1/applications/{id}/interviews")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @Operation(summary = "Schedules an interview; the candidate gets an email invitation")
    public ResponseEntity<InterviewResponse> schedule(@PathVariable UUID id, @Valid @RequestBody ScheduleRequest request) {
        Interview interview = interviews.schedule(id, request.scheduledAt(), request.durationMinutes(),
                request.interviewerIds(), request.meetingLink());
        return ResponseEntity.created(URI.create("/api/v1/interviews/" + interview.getId()))
                .body(InterviewResponse.from(new InterviewWithFeedback(interview, List.of()), currentUser.currentUser().id()));
    }

    @GetMapping("/api/v1/applications/{id}/interviews")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HIRING_MANAGER', 'ADMIN')")
    @Operation(summary = "Interviews of an application with their feedback")
    public List<InterviewResponse> forApplication(@PathVariable UUID id) {
        String me = currentUser.currentUser().id();
        return interviews.forApplication(id).stream().map(item -> InterviewResponse.from(item, me)).toList();
    }

    @GetMapping("/api/v1/interviews")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HIRING_MANAGER', 'ADMIN')")
    @Operation(summary = "Interviews in a time range (max. 62 days) for the calendar; hiring managers see their own")
    public List<CalendarEntryResponse> calendar(@RequestParam Instant from, @RequestParam Instant to) {
        return interviews.calendar(from, to).stream().map(CalendarEntryResponse::from).toList();
    }

    @DeleteMapping("/api/v1/interviews/{id}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        interviews.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/interviews/{id}/feedback")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HIRING_MANAGER', 'ADMIN')")
    @Operation(summary = "Submits the scorecard of the signed-in interviewer")
    public ResponseEntity<FeedbackResponse> feedback(@PathVariable UUID id, @Valid @RequestBody FeedbackRequest request) {
        Feedback feedback = interviews.submitFeedback(id,
                new Scores(request.technical(), request.communication(), request.cultureFit()),
                request.comment(), request.recommendation());
        return ResponseEntity.status(201).body(FeedbackResponse.from(feedback));
    }

    public record ScheduleRequest(@NotNull @Future Instant scheduledAt,
            @Min(Interview.MIN_DURATION) @Max(Interview.MAX_DURATION) int durationMinutes,
            @NotEmpty List<String> interviewerIds, @Size(max = 500) String meetingLink) {
    }

    public record FeedbackRequest(@Min(1) @Max(5) int technical, @Min(1) @Max(5) int communication,
            @Min(1) @Max(5) int cultureFit, @Size(max = Feedback.MAX_COMMENT_LENGTH) String comment,
            @NotNull Recommendation recommendation) {
    }

    public record InterviewResponse(UUID id, UUID applicationId, Instant scheduledAt, int durationMinutes,
            List<Interviewer> interviewers, String meetingLink, List<FeedbackResponse> feedback,
            boolean canGiveFeedback) {

        static InterviewResponse from(InterviewWithFeedback item, String me) {
            Interview interview = item.interview();
            boolean alreadyGiven = item.feedback().stream().anyMatch(f -> f.getAuthorId().equals(me));
            return new InterviewResponse(interview.getId(), interview.getApplicationId(), interview.getScheduledAt(),
                    interview.getDurationMinutes(), interview.getInterviewers(), interview.getMeetingLink(),
                    item.feedback().stream().map(FeedbackResponse::from).toList(),
                    interview.hasInterviewer(me) && !alreadyGiven);
        }
    }

    public record FeedbackResponse(UUID id, String authorName, int technical, int communication, int cultureFit,
            String comment, Recommendation recommendation, Instant submittedAt) {

        static FeedbackResponse from(Feedback feedback) {
            return new FeedbackResponse(feedback.getId(), feedback.getAuthorName(), feedback.getTechnical(),
                    feedback.getCommunication(), feedback.getCultureFit(), feedback.getComment(),
                    feedback.getRecommendation(), feedback.getSubmittedAt());
        }
    }

    public record CalendarEntryResponse(UUID id, UUID applicationId, Instant scheduledAt, int durationMinutes,
            List<Interviewer> interviewers, String meetingLink, String candidateName, String jobTitle) {

        static CalendarEntryResponse from(CalendarEntry entry) {
            Interview interview = entry.interview();
            return new CalendarEntryResponse(interview.getId(), interview.getApplicationId(),
                    interview.getScheduledAt(), interview.getDurationMinutes(), interview.getInterviewers(),
                    interview.getMeetingLink(), entry.candidateName(), entry.jobTitle());
        }
    }
}
