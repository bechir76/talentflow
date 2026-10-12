package com.talentflow.ai.application;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.talentflow.ai.domain.AnalysisRequest;
import com.talentflow.ai.domain.CvAnalysis;
import com.talentflow.ai.domain.PersonalDataRedactor;
import com.talentflow.application.application.CvStorage;
import com.talentflow.application.application.ScreeningRequested;
import com.talentflow.application.domain.JobApplication;
import com.talentflow.application.domain.JobApplicationRepository;
import com.talentflow.candidate.application.CandidateService;
import com.talentflow.candidate.domain.Candidate;
import com.talentflow.job.application.JobService;
import com.talentflow.job.domain.Job;
import com.talentflow.shared.domain.NotFoundException;

/**
 * Runs the AI screening of one application: extract the CV text, remove personal data, ask the analyzer
 * and store the suggestion. Any failure marks the analysis as FAILED and never affects the application.
 * The analyzer call runs outside a database transaction because it can take several seconds.
 */
@Service
public class ScreeningService {

    private static final Logger log = LoggerFactory.getLogger(ScreeningService.class);
    private static final int MAX_CV_CHARACTERS = 15_000;
    private static final int SAVE_ATTEMPTS = 2;

    private final JobApplicationRepository applications;
    private final JobService jobs;
    private final CandidateService candidates;
    private final CvStorage cvStorage;
    private final CvTextExtractor textExtractor;
    private final CvAnalyzer analyzer;
    private final PersonalDataRedactor redactor = new PersonalDataRedactor();
    private final ApplicationEventPublisher events;
    private final TransactionTemplate transaction;
    private final Clock clock;

    public ScreeningService(JobApplicationRepository applications, JobService jobs, CandidateService candidates,
            CvStorage cvStorage, CvTextExtractor textExtractor, CvAnalyzer analyzer,
            ApplicationEventPublisher events, PlatformTransactionManager transactionManager, Clock clock) {
        this.applications = applications;
        this.jobs = jobs;
        this.candidates = candidates;
        this.cvStorage = cvStorage;
        this.textExtractor = textExtractor;
        this.analyzer = analyzer;
        this.events = events;
        this.transaction = new TransactionTemplate(transactionManager);
        this.clock = clock;
    }

    /** Queues a new screening, for example after a failure or a changed job description. */
    public void requestScreening(UUID applicationId) {
        transaction.executeWithoutResult(status -> {
            JobApplication application = applications.findById(applicationId)
                    .orElseThrow(() -> new NotFoundException("Application", applicationId));
            application.requestAiAnalysis();
            events.publishEvent(new ScreeningRequested(applicationId));
        });
    }

    public void screen(UUID applicationId) {
        try {
            Optional<AnalysisRequest> request = transaction.execute(status -> buildRequest(applicationId));
            if (request == null || request.isEmpty()) {
                return;
            }
            CvAnalysis analysis = analyzer.analyze(request.get());
            saveWithRetry(applicationId, application -> application.recordAiAnalysis(analysis.score(),
                    analysis.matchingSkills(), analysis.missingSkills(), analysis.yearsOfExperience(),
                    analysis.summary(), analyzer.modelName(), clock.instant()));
            log.info("AI screening done for application {} (score {})", applicationId, analysis.score());
        } catch (RuntimeException e) {
            log.warn("AI screening failed for application {}: {}", applicationId, e.getMessage());
            saveWithRetry(applicationId, application -> application.markAiAnalysisFailed(clock.instant()));
        }
    }

    private Optional<AnalysisRequest> buildRequest(UUID applicationId) {
        Optional<JobApplication> found = applications.findById(applicationId);
        if (found.isEmpty()) {
            log.warn("Application {} no longer exists; skipping AI screening", applicationId);
            return Optional.empty();
        }
        JobApplication application = found.get();
        if (application.getCvStorageKey() == null) {
            throw new IllegalStateException("The application has no CV");
        }
        Job job = jobs.findViews(List.of(application.getJobId())).get(application.getJobId()).job();
        Candidate candidate = candidates.get(application.getCandidateId());

        String text = textExtractor.extract(cvStorage.load(application.getCvStorageKey()));
        if (text.isBlank()) {
            throw new IllegalStateException("The CV contains no extractable text");
        }
        String redacted = redactor.redact(text, candidate.getFullName(), candidate.getEmail(), candidate.getPhone());
        String limited = redacted.length() > MAX_CV_CHARACTERS ? redacted.substring(0, MAX_CV_CHARACTERS) : redacted;
        return Optional.of(new AnalysisRequest(job.getTitle(), job.getDescription(), job.getRequiredSkills(), limited));
    }

    /** A recruiter may change the stage at the same time; retry once on an optimistic lock conflict. */
    private void saveWithRetry(UUID applicationId, Consumer<JobApplication> change) {
        for (int attempt = 1; attempt <= SAVE_ATTEMPTS; attempt++) {
            try {
                transaction.executeWithoutResult(status -> applications.findById(applicationId).ifPresent(change));
                return;
            } catch (OptimisticLockingFailureException e) {
                if (attempt == SAVE_ATTEMPTS) {
                    log.error("Could not save AI screening result for application {}", applicationId, e);
                }
            }
        }
    }
}
