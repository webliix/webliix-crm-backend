-- Migration V52: Add missing columns to project_comments and projects tables

ALTER TABLE project_comments
    ADD COLUMN IF NOT EXISTS author_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS author_role VARCHAR(100);

ALTER TABLE projects
    ADD COLUMN IF NOT EXISTS documentation_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS architecture_notes TEXT;
