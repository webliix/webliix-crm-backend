CREATE TABLE ticket_attachments (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    file_name VARCHAR(255),
    file_path VARCHAR(1000),
    file_type VARCHAR(255),
    uploaded_at TIMESTAMP,
    CONSTRAINT fk_ticket_attachments_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE
);
