-- Readable actor in the audit viewer without a user directory lookup.
ALTER TABLE audit_log ADD COLUMN actor_name VARCHAR(200);
CREATE INDEX idx_audit_log_action ON audit_log (action, occurred_at DESC);

ALTER TABLE candidate ADD COLUMN anonymized_at TIMESTAMPTZ;

-- Single-row configuration for the automatic anonymisation of rejected candidates.
CREATE TABLE retention_policy (
    id                        UUID PRIMARY KEY,
    rejected_retention_months INTEGER     NOT NULL,
    auto_delete_enabled       BOOLEAN     NOT NULL,
    updated_at                TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_retention_months CHECK (rejected_retention_months BETWEEN 1 AND 120)
);

INSERT INTO retention_policy (id, rejected_retention_months, auto_delete_enabled, updated_at)
VALUES ('00000000-0000-0000-0000-000000000001', 6, FALSE, now());
