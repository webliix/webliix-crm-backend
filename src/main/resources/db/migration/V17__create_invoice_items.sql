CREATE TABLE invoice_items (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT,
    item_name VARCHAR(255),
    description TEXT,
    quantity INTEGER,
    unit_price NUMERIC(19,2),
    total_price NUMERIC(19,2),
    CONSTRAINT fk_invoice_item_invoice FOREIGN KEY(invoice_id) REFERENCES invoices(id)
);
