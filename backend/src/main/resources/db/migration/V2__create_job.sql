CREATE TABLE job (
    id              UUID PRIMARY KEY,
    version         BIGINT       NOT NULL DEFAULT 0,
    title           VARCHAR(150) NOT NULL,
    description     TEXT         NOT NULL,
    required_skills TEXT[]       NOT NULL DEFAULT '{}',
    location        VARCHAR(150) NOT NULL,
    work_mode       VARCHAR(10)  NOT NULL,
    salary_min      INTEGER,
    salary_max      INTEGER,
    currency        VARCHAR(3)   NOT NULL DEFAULT 'EUR',
    status          VARCHAR(10)  NOT NULL,
    department_id   UUID         NOT NULL REFERENCES department (id),
    created_by      VARCHAR(64)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    CONSTRAINT ck_job_work_mode CHECK (work_mode IN ('REMOTE', 'HYBRID', 'ONSITE')),
    CONSTRAINT ck_job_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'CLOSED')),
    CONSTRAINT ck_job_salary_positive CHECK (salary_min >= 0 AND salary_max >= 0),
    CONSTRAINT ck_job_salary_range CHECK (salary_min IS NULL OR salary_max IS NULL OR salary_min <= salary_max)
);

CREATE INDEX idx_job_status ON job (status);
CREATE INDEX idx_job_department ON job (department_id);
