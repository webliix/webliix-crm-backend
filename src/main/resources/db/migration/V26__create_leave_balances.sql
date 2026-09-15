CREATE TABLE leave_balances (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type VARCHAR(50) NOT NULL,
    allocation_days NUMERIC(5,2) NOT NULL,
    used_days NUMERIC(5,2) NOT NULL,
    remaining_days NUMERIC(5,2) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_leave_balance_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);
