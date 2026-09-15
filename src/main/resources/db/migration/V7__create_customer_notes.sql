CREATE TABLE customer_notes
(
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT,
    note TEXT,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    CONSTRAINT fk_customer_note_customer FOREIGN KEY(customer_id) REFERENCES customers(id)
);
