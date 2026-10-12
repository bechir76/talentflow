# TalentFlow architecture

This document describes how TalentFlow is put together: the runtime containers, the backend modules, the
data model and the most important flows. The diagrams are written in [Mermaid](https://mermaid.js.org/) and
render directly on GitHub.

## 1. System context

```mermaid
flowchart LR
    candidate([Candidate])
    recruiter([Recruiter / Hiring manager / Admin])

    subgraph talentflow[TalentFlow]
        web[Web app<br/>React + Vite, served by nginx]
        api[Backend API<br/>Spring Boot 4, Java 25]
    end

    keycloak[(Keycloak<br/>identity provider)]
    db[(PostgreSQL 17)]
    files[(CV storage<br/>volume)]
    smtp[SMTP server<br/>Mailpit locally]
    llm[LLM provider<br/>OpenAI-compatible API, optional]

    candidate -->|career page, apply, privacy self-service| web
    recruiter -->|jobs, pipeline, interviews, admin| web
    web -->|OIDC login with PKCE| keycloak
    web -->|REST + JSON, Bearer token| api
    api -->|validates JWT signature, issuer, audience| keycloak
    api -->|Admin REST API, service account| keycloak
    api --> db
    api --> files
    api -->|stage and interview e-mails| smtp
    api -->|redacted CV text + job| llm
```

| Container | Port (local) | Responsibility |
|---|---|---|
| `frontend` | 3000 | nginx serves the built React app and proxies `/api` to the backend |
| `backend` | 8080 (`BACKEND_PORT`) | REST API, business rules, AI screening, e-mails, scheduled retention job |
| `keycloak` | 8180 | Login, self-registration, roles; realm imported from `infra/keycloak/realm-export.json` |
| `postgres` | 5432 | All application data; schema managed by Flyway |
| `mailpit` | 8025 / 1025 | Catches all outgoing e-mail in development |

## 2. Backend modules (hexagonal architecture)

Every module has the same three layers:

- `domain`: entities, value objects and rules, plus repository ports (interfaces). No web code.
- `application`: use cases (services), ports to other systems, events.
- `infrastructure`: adapters: REST controllers, JPA repositories, Keycloak and LLM clients, mail, files.

Dependencies always point inwards (infrastructure → application → domain). Where two modules would depend on
each other, the inner one defines a port and the outer one implements it, for example `ApplicantCounter`
(job → implemented by application) and `InterviewAssignments` (application → implemented by interview).

```mermaid
flowchart TB
    subgraph modules[com.talentflow]
        job[job<br/>jobs, departments, career page]
        application[application<br/>job applications, CV files]
        candidate[candidate<br/>profiles, consents]
        ai[ai<br/>PDF text, redaction, analyzers]
        pipeline[pipeline<br/>stages, history, dashboard, e-mails]
        interview[interview<br/>scheduling, feedback]
        gdpr[gdpr<br/>export, erasure, retention]
        audit[audit<br/>audit trail]
        user[user<br/>current user, user admin via Keycloak]
        shared[shared<br/>security, errors, roles, config]
    end

    application --> job
    application --> candidate
    ai --> application
    ai --> job
    ai --> candidate
    pipeline --> application
    pipeline --> audit
    pipeline --> interview
    interview --> application
    interview --> user
    gdpr --> application
    gdpr --> candidate
    gdpr --> pipeline
    gdpr --> interview
    gdpr --> audit
    user --> audit
    job -. "ApplicantCounter port" .-> application
    application -. "InterviewAssignments port" .-> interview
```

All modules use `shared` (roles, `CurrentUserProvider`, Problem Details error handling, security config).

## 3. Data model

```mermaid
erDiagram
    DEPARTMENT ||--o{ JOB : has
    JOB ||--o{ JOB_APPLICATION : receives
    CANDIDATE ||--o{ JOB_APPLICATION : submits
    CANDIDATE ||--o{ CONSENT : gives
    JOB_APPLICATION ||--o{ STAGE_CHANGE : "moves through"
    JOB_APPLICATION ||--o{ INTERVIEW : has
    INTERVIEW ||--o{ FEEDBACK : "gets"

    JOB {
        uuid id
        string title
        text description
        string[] required_skills
        string work_mode "REMOTE | HYBRID | ONSITE"
        int salary_min
        int salary_max
        string status "DRAFT | PUBLISHED | CLOSED"
    }
    CANDIDATE {
        uuid id
        string keycloak_user_id
        string full_name
        string email
        boolean anonymized
    }
    CONSENT {
        uuid id
        string type
        string policy_version
        timestamp given_at
        timestamp withdrawn_at
    }
    JOB_APPLICATION {
        uuid id
        string cv_storage_key
        string stage "APPLIED ... HIRED | REJECTED"
        int ai_score
        string[] ai_matching_skills
        string[] ai_missing_skills
        string ai_status "PENDING | DONE | FAILED"
        string ai_model
    }
    STAGE_CHANGE {
        uuid id
        string from_stage
        string to_stage
        string changed_by
        timestamp changed_at
    }
    INTERVIEW {
        uuid id
        timestamp scheduled_at
        int duration_minutes
        string[] interviewer_ids
        string meeting_link
    }
    FEEDBACK {
        uuid id
        int technical
        int communication
        int culture_fit
        string recommendation "HIRE | NO_HIRE | UNSURE"
    }
```

`audit_log` (actor, action, entity, time, details) and `retention_policy` (single row) are standalone tables.

## 4. Key flows

### 4.1 Applying and AI screening

The screening runs after the application is committed, on a virtual thread, and never blocks or rolls back the
application. The LLM call happens outside a database transaction.

```mermaid
sequenceDiagram
    actor C as Candidate
    participant W as Web app
    participant A as ApplyForJobService
    participant DB as PostgreSQL
    participant FS as CV storage
    participant S as ScreeningService (async)
    participant L as CvAnalyzer (LLM or keyword matcher)

    C->>W: Upload PDF, tick consent, submit
    W->>A: POST /jobs/{id}/applications (multipart)
    A->>A: check consent, published job, no duplicate, PDF signature
    A->>FS: store CV under random key
    A->>DB: application (PENDING) + consent with policy version
    A-->>W: 201 Created
    Note over A,S: ScreeningRequested event, handled AFTER_COMMIT
    S->>FS: load CV
    S->>S: extract text (PDFBox), remove name, e-mail, phone, links
    S->>L: job + redacted CV text
    L-->>S: strict JSON {score, matchingSkills, missingSkills, yearsOfExperience, summary}
    S->>S: validate contract
    S->>DB: store suggestion (DONE) or mark FAILED
```

### 4.2 Moving a candidate in the pipeline

```mermaid
sequenceDiagram
    actor R as Recruiter
    participant K as Kanban board
    participant P as PipelineService
    participant DB as PostgreSQL
    participant M as Mail (async)

    R->>K: drag card to "Interview"
    K->>K: optimistic update
    K->>P: PATCH /applications/{id}/stage
    P->>P: domain rule check (e.g. HIRED only from OFFER)
    P->>DB: update stage, insert stage_change, insert audit_log (one transaction)
    P-->>K: 200 (or 422 → card moves back)
    Note over P,M: StageChanged event, AFTER_COMMIT
    M->>M: bilingual e-mail to the candidate
```

### 4.3 Right to erasure

`CandidateErasureService` is shared by the candidate's own request and the daily retention job:

1. Candidate profile: name, e-mail, phone, location and the link to the login account are replaced or removed.
2. Applications: CV reference, cover letter and AI output derived from the CV are cleared. Stage and dates stay
   so anonymous statistics (funnel, time to hire) remain correct.
3. Interviews and feedback: meeting links and free-text comments are cleared.
4. Consents are marked as withdrawn.
5. CV files are deleted **after** the transaction commits, so a rollback never loses a file.
6. An audit entry records who triggered the erasure (the candidate or the retention job).

## 5. Security model

- Login happens only at Keycloak (authorization code flow with PKCE). The web app never sees passwords.
- The backend is a stateless OAuth2 resource server. It checks signature, issuer and audience (`talentflow-api`)
  and maps Keycloak realm roles to Spring roles.
- Endpoints are protected with `@PreAuthorize`. Row-level rules live in the application layer, for example
  hiring managers only see applications they interview (`ApplicationAccessGuard`).
- Errors, including 401 and 403, are RFC 9457 Problem Details.
- User administration uses a separate confidential Keycloak client (service account) whose secret comes from the
  environment.

## 6. Concurrency

`JobApplication` is written by two parties: recruiters (stage) and the screening job (AI fields).

- The AI fields are excluded from optimistic locking, so a finishing screening never makes a stage change fail.
- Updates are dynamic (only changed columns), so neither side can overwrite the other's fields with stale values.
- The screening retries once if a stage change happened while it was saving.
