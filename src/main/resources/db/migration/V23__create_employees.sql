CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    employee_code VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),

    department_id BIGINT,
    designation_id BIGINT,

    joining_date DATE,
    salary NUMERIC(19,2),
    employment_type VARCHAR(50),

    active BOOLEAN NOT NULL DEFAULT TRUE,

    address TEXT,
    city VARCHAR(255),
    state VARCHAR(255),
    country VARCHAR(255),

    emergency_contact VARCHAR(255),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_employee_department
        FOREIGN KEY (department_id)
        REFERENCES departments(id),

    CONSTRAINT fk_employee_designation
        FOREIGN KEY (designation_id)
        REFERENCES designations(id)
);