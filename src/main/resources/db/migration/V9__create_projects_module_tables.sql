CREATE TABLE projects
(
    id BIGSERIAL PRIMARY KEY,
    project_code VARCHAR(50) UNIQUE,
    project_name VARCHAR(255),
    description TEXT,
    budget NUMERIC(19,2),
    start_date DATE,
    expected_end_date DATE,
    actual_end_date DATE,
    status VARCHAR(50),
    priority VARCHAR(50),
    customer_id BIGINT,
    progress_percentage INTEGER,
    billable BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_project_customer FOREIGN KEY(customer_id) REFERENCES customers(id)
);

CREATE TABLE project_members
(
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT,
    user_id BIGINT,
    role_in_project VARCHAR(255),
    assigned_date DATE,
    CONSTRAINT fk_project_member_project FOREIGN KEY(project_id) REFERENCES projects(id)
);

CREATE TABLE project_milestones
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

CREATE TABLE project_tasks
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

CREATE TABLE project_comments
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
