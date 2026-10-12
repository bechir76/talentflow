CREATE TABLE interview (
    id                UUID PRIMARY KEY,
    application_id    UUID         NOT NULL REFERENCES job_application (id),
    scheduled_at      TIMESTAMPTZ  NOT NULL,
    duration_minutes  INTEGER      NOT NULL,
    -- Keycloak user ids and display names of the interviewers, in the same order.
    interviewer_ids   TEXT[]       NOT NULL,
    interviewer_names TEXT[]       NOT NULL,
    meeting_link      VARCHAR(500),
    created_by        VARCHAR(64)  NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL,
    CONSTRAINT ck_interview_duration CHECK (duration_minutes BETWEEN 15 AND 480)
);

CREATE INDEX idx_interview_scheduled_at ON interview (scheduled_at);
CREATE INDEX idx_interview_application ON interview (application_id);

CREATE TABLE feedback (
    id             UUID PRIMARY KEY,
    interview_id   UUID         NOT NULL REFERENCES interview (id) ON DELETE CASCADE,
    author_id      VARCHAR(64)  NOT NULL,
    author_name    VARCHAR(200),
    technical      INTEGER      NOT NULL,
    communication  INTEGER      NOT NULL,
    culture_fit    INTEGER      NOT NULL,
    comment        TEXT,
    recommendation VARCHAR(10)  NOT NULL,
    submitted_at   TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_feedback_interview_author UNIQUE (interview_id, author_id),
    CONSTRAINT ck_feedback_scores CHECK (technical BETWEEN 1 AND 5 AND communication BETWEEN 1 AND 5
        AND culture_fit BETWEEN 1 AND 5),
    CONSTRAINT ck_feedback_recommendation CHECK (recommendation IN ('HIRE', 'NO_HIRE', 'UNSURE'))
);
