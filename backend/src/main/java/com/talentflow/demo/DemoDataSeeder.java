package com.talentflow.demo;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.talentflow.ai.domain.AnalysisRequest;
import com.talentflow.ai.domain.CvAnalysis;
import com.talentflow.ai.domain.PersonalDataRedactor;
import com.talentflow.ai.infrastructure.analyzer.KeywordCvAnalyzer;
import com.talentflow.ai.infrastructure.pdf.PdfBoxCvTextExtractor;
import com.talentflow.application.application.CvStorage;
import com.talentflow.application.domain.ApplicationStage;
import com.talentflow.application.domain.JobApplication;
import com.talentflow.application.domain.JobApplicationRepository;
import com.talentflow.audit.domain.AuditAction;
import com.talentflow.audit.domain.AuditLog;
import com.talentflow.audit.domain.AuditLogRepository;
import com.talentflow.candidate.domain.Candidate;
import com.talentflow.candidate.domain.CandidateRepository;
import com.talentflow.candidate.domain.Consent;
import com.talentflow.candidate.domain.ConsentType;
import com.talentflow.demo.DemoData.ApplicationSpec;
import com.talentflow.demo.DemoData.JobSpec;
import com.talentflow.demo.DemoData.Person;
import com.talentflow.interview.domain.Feedback;
import com.talentflow.interview.domain.Feedback.Scores;
import com.talentflow.interview.domain.Interview;
import com.talentflow.interview.domain.Interview.Interviewer;
import com.talentflow.interview.domain.InterviewRepository;
import com.talentflow.interview.domain.Recommendation;
import com.talentflow.job.domain.Department;
import com.talentflow.job.domain.DepartmentRepository;
import com.talentflow.job.domain.Job;
import com.talentflow.job.domain.JobDetails;
import com.talentflow.job.domain.JobRepository;
import com.talentflow.job.domain.JobSearchCriteria;
import com.talentflow.job.domain.JobStatus;
import com.talentflow.pipeline.domain.StageChange;
import com.talentflow.pipeline.domain.StageChangeRepository;

/**
 * Fills an empty database with fictional demo data: jobs in every status, candidates in all pipeline stages with
 * generated PDF CVs and AI suggestions, stage history, interviews with scorecards and audit entries.
 * Enabled with {@code SEED_DEMO_DATA=true}; it never runs when jobs already exist.
 */
@Component
@ConditionalOnProperty(name = "talentflow.demo.seed", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final Interviewer MARTIN = new Interviewer(DemoData.MANAGER_USER, DemoData.MANAGER_NAME);
    private static final Interviewer ANNA = new Interviewer(DemoData.RECRUITER_USER, DemoData.RECRUITER_NAME);

    private final JobRepository jobs;
    private final DepartmentRepository departments;
    private final CandidateRepository candidates;
    private final JobApplicationRepository applications;
    private final StageChangeRepository stageChanges;
    private final InterviewRepository interviews;
    private final AuditLogRepository auditLog;
    private final CvStorage cvStorage;
    private final TransactionTemplate transaction;
    private final Clock clock;
    private final ZoneId zone;
    private final String policyVersion;
    private final PdfBoxCvTextExtractor extractor = new PdfBoxCvTextExtractor();
    private final PersonalDataRedactor redactor = new PersonalDataRedactor();
    private final KeywordCvAnalyzer analyzer;

    public DemoDataSeeder(JobRepository jobs, DepartmentRepository departments, CandidateRepository candidates,
            JobApplicationRepository applications, StageChangeRepository stageChanges, InterviewRepository interviews,
            AuditLogRepository auditLog, CvStorage cvStorage, PlatformTransactionManager transactionManager, Clock clock,
            @Value("${talentflow.time-zone}") String zone, @Value("${talentflow.privacy.policy-version}") String policyVersion) {
        this.jobs = jobs;
        this.departments = departments;
        this.candidates = candidates;
        this.applications = applications;
        this.stageChanges = stageChanges;
        this.interviews = interviews;
        this.auditLog = auditLog;
        this.cvStorage = cvStorage;
        this.transaction = new TransactionTemplate(transactionManager);
        this.clock = clock;
        this.zone = ZoneId.of(zone);
        this.policyVersion = policyVersion;
        this.analyzer = new KeywordCvAnalyzer(clock);
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!jobs.search(new JobSearchCriteria(null, null, null, null), PageRequest.of(0, 1)).isEmpty()) {
            log.info("Demo data skipped: the database already contains jobs");
            return;
        }
        transaction.executeWithoutResult(status -> seed());
        log.info("Demo data created: {} jobs, {} applications", DemoData.JOBS.size(), DemoData.APPLICATIONS.size());
    }

    private void seed() {
        Instant now = clock.instant();
        Map<String, Job> jobsByKey = createJobs(now);
        Map<String, Candidate> people = new HashMap<>();
        Map<String, JobApplication> byPerson = new HashMap<>();
        for (ApplicationSpec spec : DemoData.APPLICATIONS) {
            Candidate candidate = people.computeIfAbsent(spec.person().email(), email -> createCandidate(spec, now));
            Job job = jobsByKey.get(spec.jobKey());
            JobApplication application = createApplication(spec, candidate, job, now);
            byPerson.put(spec.person().name() + "/" + spec.jobKey(), application);
        }
        createInterviews(byPerson, now);
    }

    private Map<String, Job> createJobs(Instant now) {
        Map<String, UUID> departmentIds = new HashMap<>();
        for (Department department : departments.findAllOrderedByName()) {
            departmentIds.put(department.getName(), department.getId());
        }
        Map<String, Job> created = new HashMap<>();
        for (JobSpec spec : DemoData.JOBS) {
            JobDetails details = new JobDetails(spec.title(), spec.description(), spec.skills(), spec.location(),
                    spec.workMode(), spec.salaryMin(), spec.salaryMax(), departmentIds.get(spec.department()));
            Instant posted = now.minus(Duration.ofDays(spec.postedDaysAgo()));
            Job job = Job.create(details, spec.status() == JobStatus.DRAFT ? JobStatus.DRAFT : JobStatus.PUBLISHED,
                    DemoData.RECRUITER_USER, posted);
            if (spec.status() == JobStatus.CLOSED) {
                job.changeStatus(JobStatus.CLOSED, posted.plus(Duration.ofDays(30)));
            }
            created.put(spec.key(), jobs.save(job));
        }
        return created;
    }

    private Candidate createCandidate(ApplicationSpec spec, Instant now) {
        Person person = spec.person();
        Instant createdAt = now.minus(Duration.ofDays(spec.appliedDaysAgo())).minus(Duration.ofHours(1));
        Candidate candidate = Candidate.register(spec.linkedToDemoAccount() ? DemoData.CANDIDATE_USER : null,
                person.name(), person.email(), createdAt);
        candidate.updateContactDetails(person.name(), person.email(), person.phone(), person.city());
        return candidates.save(candidate);
    }

    private JobApplication createApplication(ApplicationSpec spec, Candidate candidate, Job job, Instant now) {
        Instant appliedAt = now.minus(Duration.ofDays(spec.appliedDaysAgo())).minus(Duration.ofHours(spec.appliedDaysAgo() % 7 + 2));
        byte[] pdf = DemoCvRenderer.render(spec.person());
        JobApplication application = JobApplication.submit(job.getId(), candidate.getId(), cvStorage.store(pdf),
                spec.coverLetter(), appliedAt);
        candidates.save(Consent.give(candidate.getId(), ConsentType.APPLICATION_PROCESSING, policyVersion, appliedAt));

        String text = redactor.redact(extractor.extract(pdf), candidate.getFullName(), candidate.getEmail(), candidate.getPhone());
        CvAnalysis analysis = analyzer.analyze(new AnalysisRequest(job.getTitle(), job.getDescription(), job.getRequiredSkills(), text));
        application.recordAiAnalysis(analysis.score(), analysis.matchingSkills(), analysis.missingSkills(),
                analysis.yearsOfExperience(), analysis.summary(), analyzer.modelName(), appliedAt.plus(Duration.ofMinutes(1)));
        applications.save(application);

        List<ApplicationStage> path = spec.path();
        Duration step = Duration.between(appliedAt, now).dividedBy(path.size() + 1);
        for (int i = 0; i < path.size(); i++) {
            Instant changedAt = appliedAt.plus(step.multipliedBy(i + 1));
            ApplicationStage from = application.moveTo(path.get(i), changedAt);
            stageChanges.save(StageChange.record(application.getId(), from, path.get(i), DemoData.RECRUITER_USER,
                    DemoData.RECRUITER_NAME, null, changedAt));
            auditLog.save(AuditLog.of(DemoData.RECRUITER_USER, DemoData.RECRUITER_NAME, AuditAction.STAGE_CHANGED,
                    "JobApplication", application.getId(), from + " -> " + path.get(i), changedAt));
        }
        return applications.save(application);
    }

    private void createInterviews(Map<String, JobApplication> byPerson, Instant now) {
        upcoming(byPerson.get("Lena Schmidt/java"), now, 1, LocalTime.of(10, 0), 60, List.of(MARTIN, ANNA));
        upcoming(byPerson.get("Sophia Müller/frontend"), now, 2, LocalTime.of(14, 0), 45, List.of(MARTIN));
        upcoming(byPerson.get("Hannah Becker/devops"), now, 3, LocalTime.of(11, 30), 60, List.of(MARTIN));
        upcoming(byPerson.get("Tim Schulz/design"), now, 1, LocalTime.of(15, 30), 45, List.of(ANNA));
        past(byPerson.get("Jonas Weber/java"), now, 4, new Scores(5, 4, 4), Recommendation.HIRE,
                "Deep Java and AWS knowledge, explained trade-offs clearly. Would strengthen the platform team.");
        past(byPerson.get("Elisa Fischer/java"), now, 15, new Scores(5, 5, 4), Recommendation.HIRE,
                "Excellent system design and mentoring experience.");
        past(byPerson.get("Nina Kowalski/data"), now, 3, new Scores(4, 4, 3), Recommendation.UNSURE,
                "Strong SQL; would like to see more stakeholder work in a second conversation.");
    }

    private void upcoming(JobApplication application, Instant now, int inDays, LocalTime time, int minutes,
            List<Interviewer> interviewers) {
        Instant start = now.atZone(zone).toLocalDate().plusDays(inDays).atTime(time).atZone(zone).toInstant();
        interviews.save(Interview.schedule(application.getId(), start, minutes, interviewers,
                "https://meet.example.org/talentflow-" + application.getId().toString().substring(0, 8),
                DemoData.RECRUITER_USER, now));
    }

    private void past(JobApplication application, Instant now, int daysAgo, Scores scores, Recommendation recommendation,
            String comment) {
        Instant start = now.minus(Duration.ofDays(daysAgo)).truncatedTo(ChronoUnit.HOURS);
        Interview interview = interviews.save(Interview.schedule(application.getId(), start, 60, List.of(MARTIN), null,
                DemoData.RECRUITER_USER, start.minus(Duration.ofDays(2))));
        interviews.save(Feedback.submit(interview.getId(), DemoData.MANAGER_USER, DemoData.MANAGER_NAME, scores, comment,
                recommendation, start.plus(Duration.ofHours(2))));
    }
}
