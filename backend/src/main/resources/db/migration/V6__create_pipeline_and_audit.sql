-- When the application entered its current stage ("3 days in stage" on the Kanban board).
ALTER TABLE job_application ADD COLUMN stage_changed_at TIMESTAMPTZ;
UPDATE job_application SET stage_changed_at = applied_at;
ALTER TABLE job_application ALTER COLUMN stage_changed_at SET NOT NULL;

CREATE INDEX idx_job_application_job ON job_application (job_id);

CREATE TABLE stage_change (
    id              UUID PRIMARY KEY,
    application_id  UUID         NOT NULL REFERENCES job_application (id),
    from_stage      VARCHAR(20)  NOT NULL,
    to_stage        VARCHAR(20)  NOT NULL,
    changed_by      VARCHAR(64)  NOT NULL,
    changed_by_name VARCHAR(200),
    changed_at      TIMESTAMPTZ  NOT NULL,
    note            VARCHAR(500)
);

CREATE INDEX idx_stage_change_application ON stage_change (application_id, changed_at);

-- Append-only record of sensitive actions (GDPR accountability, Art. 5(2)).
CREATE TABLE audit_log (
    id          UUID PRIMARY KEY,
    actor_id    VARCHAR(64) NOT NULL,
    action      VARCHAR(40) NOT NULL,
    entity_type VARCHAR(40) NOT NULL,
    entity_id   UUID,
    occurred_at TIMESTAMPTZ NOT NULL,
    details     TEXT
);

CREATE INDEX idx_audit_log_occurred_at ON audit_log (occurred_at DESC);
