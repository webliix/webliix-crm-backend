CREATE TABLE customer_contacts
(
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT,
    name VARCHAR(255),
    designation VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    is_primary BOOLEAN,
    CONSTRAINT fk_customer_contact_customer FOREIGN KEY(customer_id) REFERENCES customers(id)
);
