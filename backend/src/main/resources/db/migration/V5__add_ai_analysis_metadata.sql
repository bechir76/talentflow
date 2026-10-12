-- Extra fields from the AI screening result. The model name and time are kept for transparency
-- (EU AI Act): recruiters can see which system produced a suggestion and when.
ALTER TABLE job_application
    ADD COLUMN ai_years_of_experience INTEGER,
    ADD COLUMN ai_model               VARCHAR(100),
    ADD COLUMN ai_analyzed_at         TIMESTAMPTZ,
    ADD CONSTRAINT ck_job_application_ai_years CHECK (ai_years_of_experience >= 0);
