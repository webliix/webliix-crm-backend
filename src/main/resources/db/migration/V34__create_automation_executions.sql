-- Recreated migration for automation_executions table
CREATE TABLE IF NOT EXISTS automation_executions
(
    id BIGSERIAL PRIMARY KEY,
    rule_id BIGINT,
    status VARCHAR(50) NOT NULL,
    execution_time TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP
);
