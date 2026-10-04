CREATE TABLE IF NOT EXISTS work_logs (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    log_date DATE NOT NULL,
    work_summary TEXT NOT NULL,
    hours_worked NUMERIC(4,2),
    project_id BIGINT,
    task_id BIGINT,
    tasks_completed TEXT,
    blockers TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED',
    reviewed_by VARCHAR(255),
    review_notes TEXT,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_work_logs_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_work_logs_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL,
    CONSTRAINT fk_work_logs_task FOREIGN KEY (task_id) REFERENCES project_tasks(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_work_logs_employee_id ON work_logs(employee_id);
CREATE INDEX IF NOT EXISTS idx_work_logs_log_date ON work_logs(log_date);
CREATE INDEX IF NOT EXISTS idx_work_logs_project_id ON work_logs(project_id);
CREATE INDEX IF NOT EXISTS idx_work_logs_status ON work_logs(status);
