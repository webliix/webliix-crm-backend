CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,

    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,

    recipient VARCHAR(255) NOT NULL,
    recipient_type VARCHAR(50) NOT NULL,

    channel VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,

    reference_type VARCHAR(50),
    reference_id BIGINT,

    sent_at TIMESTAMP,
    read_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_recipient
    ON notifications(recipient);

CREATE INDEX idx_notifications_status
    ON notifications(status);

CREATE INDEX idx_notifications_created_at
    ON notifications(created_at);

CREATE INDEX idx_notifications_reference
    ON notifications(reference_type, reference_id);