CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    payment_number VARCHAR(100) NOT NULL UNIQUE,
    invoice_id BIGINT,
    customer_id BIGINT,
    amount NUMERIC(19,2),
    payment_date DATE,
    payment_method VARCHAR(50),
    status VARCHAR(50),
    transaction_reference VARCHAR(255),
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_invoice
        FOREIGN KEY (invoice_id)
        REFERENCES invoices(id),

    CONSTRAINT fk_payment_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id)
);