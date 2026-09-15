CREATE TABLE attendance (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,

    attendance_date DATE NOT NULL,

    check_in_time TIMESTAMP,
    check_out_time TIMESTAMP,

    working_hours NUMERIC(5,2),

    status VARCHAR(50) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_attendance_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(id)
);