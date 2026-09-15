CREATE TABLE quotation_items
(
    id BIGSERIAL PRIMARY KEY,
    quotation_id BIGINT,
    item_name VARCHAR(255),
    description TEXT,
    quantity INTEGER,
    unit_price NUMERIC(19,2),
    total_price NUMERIC(19,2),
    CONSTRAINT fk_qitem_quotation FOREIGN KEY(quotation_id) REFERENCES quotations(id)
);
