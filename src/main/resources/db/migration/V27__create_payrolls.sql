CREATE TABLE payrolls (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    payroll_month VARCHAR(20) NOT NULL,
    gross_salary NUMERIC(19,2) NOT NULL,
    deductions NUMERIC(19,2) NOT NULL,
    net_salary NUMERIC(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payroll_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);
