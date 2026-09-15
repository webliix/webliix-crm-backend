-- Recreated migration for project_tasks table
CREATE TABLE IF NOT EXISTS project_tasks
(
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT,
    title VARCHAR(255),
    description TEXT,
    status VARCHAR(50),
    assigned_to BIGINT,
    start_date DATE,
    due_date DATE,
    completed_at TIMESTAMP,
    CONSTRAINT fk_project_task_project FOREIGN KEY(project_id) REFERENCES projects(id)
);
