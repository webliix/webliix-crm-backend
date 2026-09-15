CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    expense_number VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50),
    description TEXT,
    amount NUMERIC(19,2),
    expense_date DATE,
    payment_method VARCHAR(50),
    vendor VARCHAR(255),
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);