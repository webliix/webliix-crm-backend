CREATE TABLE leads
(
    id BIGSERIAL PRIMARY KEY,
    company_name VARCHAR(255),
    contact_person VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    website VARCHAR(255),
    address VARCHAR(512),
    city VARCHAR(255),
    state VARCHAR(255),
    country VARCHAR(255),
    requirements TEXT,
    estimated_value NUMERIC(19,2),
    status VARCHAR(50),
    source VARCHAR(50),
    next_follow_up_date DATE,
    notes TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
