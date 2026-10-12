CREATE TABLE candidate (
    id               UUID PRIMARY KEY,
    keycloak_user_id VARCHAR(64),
    full_name        VARCHAR(200) NOT NULL,
    email            VARCHAR(254) NOT NULL,
    phone            VARCHAR(40),
    location         VARCHAR(150),
    anonymized       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_candidate_keycloak_user UNIQUE (keycloak_user_id)
);

-- Proof of consent (GDPR Art. 7): who agreed to which policy version, and when.
CREATE TABLE consent (
    id             UUID PRIMARY KEY,
    candidate_id   UUID        NOT NULL REFERENCES candidate (id),
    type           VARCHAR(40) NOT NULL,
    policy_version VARCHAR(20) NOT NULL,
    given_at       TIMESTAMPTZ NOT NULL,
    withdrawn_at   TIMESTAMPTZ
);

CREATE INDEX idx_consent_candidate ON consent (candidate_id);

CREATE TABLE job_application (
    id                 UUID PRIMARY KEY,
    version            BIGINT      NOT NULL DEFAULT 0,
    job_id             UUID        NOT NULL REFERENCES job (id),
    candidate_id       UUID        NOT NULL REFERENCES candidate (id),
    -- Nullable: the file is deleted when the candidate exercises the right to erasure.
    cv_storage_key     VARCHAR(100),
    cover_letter       TEXT,
    stage              VARCHAR(20) NOT NULL,
    ai_score           INTEGER,
    ai_matching_skills TEXT[]      NOT NULL DEFAULT '{}',
    ai_missing_skills  TEXT[]      NOT NULL DEFAULT '{}',
    ai_summary         TEXT,
    ai_status          VARCHAR(10) NOT NULL,
    applied_at         TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_job_application_job_candidate UNIQUE (job_id, candidate_id),
    CONSTRAINT ck_job_application_stage
        CHECK (stage IN ('APPLIED', 'SCREENING', 'INTERVIEW', 'OFFER', 'HIRED', 'REJECTED')),
    CONSTRAINT ck_job_application_ai_status CHECK (ai_status IN ('PENDING', 'DONE', 'FAILED')),
    CONSTRAINT ck_job_application_ai_score CHECK (ai_score BETWEEN 0 AND 100)
);

CREATE INDEX idx_job_application_candidate ON job_application (candidate_id);
