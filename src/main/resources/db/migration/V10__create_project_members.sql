-- Recreated migration for project_members table
CREATE TABLE IF NOT EXISTS project_members
(
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT,
    user_id BIGINT,
    role_in_project VARCHAR(255),
    assigned_date DATE,
    CONSTRAINT fk_project_member_project FOREIGN KEY(project_id) REFERENCES projects(id)
);
