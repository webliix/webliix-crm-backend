-- Mobile Devices
CREATE TABLE IF NOT EXISTS mobile_devices (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    user_id BIGINT,
    device_id VARCHAR(255),
    device_type VARCHAR(100),
    os_version VARCHAR(100),
    app_version VARCHAR(100),
    fcm_token VARCHAR(512),
    last_active_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
 
-- Notification Preferences
CREATE TABLE IF NOT EXISTS notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    user_id BIGINT,
    email_enabled BOOLEAN DEFAULT TRUE,
    push_enabled BOOLEAN DEFAULT TRUE,
    sms_enabled BOOLEAN DEFAULT FALSE,
    in_app_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Refresh Tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    token VARCHAR(512) NOT NULL,
    device_id VARCHAR(255),
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sync Events
CREATE TABLE IF NOT EXISTS sync_events (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(150),
    entity_id VARCHAR(255),
    operation VARCHAR(50),
    payload TEXT,
    event_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT,
    device_id VARCHAR(255)
);