CREATE TABLE designations (
    id BIGSERIAL PRIMARY KEY,
    designation_code VARCHAR(100) NOT NULL UNIQUE,
    designation_name VARCHAR(255) NOT NULL,
    department_id BIGINT,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_designation_department
        FOREIGN KEY (department_id)
        REFERENCES departments(id)
);