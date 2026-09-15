-- Recreated migration for project_milestones table
CREATE TABLE IF NOT EXISTS project_milestones
(
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT,
    title VARCHAR(255),
    description TEXT,
    due_date DATE,
    completed BOOLEAN,
    completed_at TIMESTAMP,
    CONSTRAINT fk_project_milestone_project FOREIGN KEY(project_id) REFERENCES projects(id)
);
