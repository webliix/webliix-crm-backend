CREATE TABLE ticket_comments (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    comment TEXT,
    commented_by VARCHAR(255),
    created_at TIMESTAMP,
    CONSTRAINT fk_ticket_comments_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE
);
