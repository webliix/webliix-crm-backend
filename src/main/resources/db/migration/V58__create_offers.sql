-- Create offers table for promotions and discounts
CREATE TABLE IF NOT EXISTS offers (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    code VARCHAR(100) NOT NULL,
    discount VARCHAR(100) NOT NULL,
    badge VARCHAR(100),
    badge_color VARCHAR(50) DEFAULT 'primary',
    features TEXT,
    expires_at VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_offers_active ON offers(active);
