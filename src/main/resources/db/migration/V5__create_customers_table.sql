CREATE TABLE customers
(
    id BIGSERIAL PRIMARY KEY,
    company_name VARCHAR(255),
    customer_code VARCHAR(50) UNIQUE,
    contact_person VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    website VARCHAR(255),
    gst_number VARCHAR(100),
    address VARCHAR(512),
    city VARCHAR(255),
    state VARCHAR(255),
    country VARCHAR(255),
    lifetime_value NUMERIC(19,2),
    customer_since DATE,
    active BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
