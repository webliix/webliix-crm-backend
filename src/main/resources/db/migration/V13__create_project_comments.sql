-- Recreated migration for project_comments table
CREATE TABLE IF NOT EXISTS project_comments
(
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT,
    task_id BIGINT,
    author_id BIGINT,
    message TEXT,
    created_at TIMESTAMP,
    CONSTRAINT fk_project_comment_project FOREIGN KEY(project_id) REFERENCES projects(id),
    CONSTRAINT fk_project_comment_task FOREIGN KEY(task_id) REFERENCES project_tasks(id)
);
