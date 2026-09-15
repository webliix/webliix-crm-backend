CREATE TABLE quotations
(
    id BIGSERIAL PRIMARY KEY,
    quotation_number VARCHAR(100) UNIQUE NOT NULL,
    customer_id BIGINT,
    project_id BIGINT,
    issue_date DATE,
    valid_till DATE,
    subtotal NUMERIC(19,2),
    tax_amount NUMERIC(19,2),
    discount NUMERIC(19,2),
    total_amount NUMERIC(19,2),
    status VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_quotation_customer FOREIGN KEY(customer_id) REFERENCES customers(id),
    CONSTRAINT fk_quotation_project FOREIGN KEY(project_id) REFERENCES projects(id)
);
