-- V64: Synchronize database schema with application entities for production validation
-- Fixes missing columns in leads, blog_posts, sync_events, work_logs, and creates missing tables user_api_tokens, mobile_notification_preferences

-- 1. LEADS: Add missing creator and department columns
ALTER TABLE leads ADD COLUMN IF NOT EXISTS created_by_user_id BIGINT;
ALTER TABLE leads ADD COLUMN IF NOT EXISTS department_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_leads_created_by_user') THEN
        ALTER TABLE leads ADD CONSTRAINT fk_leads_created_by_user FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_leads_department') THEN
        ALTER TABLE leads ADD CONSTRAINT fk_leads_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_leads_created_by_user_id ON leads(created_by_user_id);
CREATE INDEX IF NOT EXISTS idx_leads_department_id ON leads(department_id);
CREATE INDEX IF NOT EXISTS idx_leads_assigned_to ON leads(assigned_to);

-- 2. BLOG POSTS: Add missing advertising and cover image layout columns
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS enable_ads BOOLEAN DEFAULT TRUE;
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS adsense_client_id VARCHAR(100);
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS top_ad_slot_id VARCHAR(100);
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS inline_ad_slot_id VARCHAR(100);
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS bottom_ad_slot_id VARCHAR(100);
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS ad_format VARCHAR(50);
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS cover_image_alignment VARCHAR(50);
ALTER TABLE blog_posts ADD COLUMN IF NOT EXISTS cover_image_aspect_ratio VARCHAR(50);

-- 3. SYNC EVENTS: Add timestamp column mapped in SyncEvent entity
ALTER TABLE sync_events ADD COLUMN IF NOT EXISTS timestamp TIMESTAMP;
UPDATE sync_events SET timestamp = event_timestamp WHERE timestamp IS NULL AND event_timestamp IS NOT NULL;

-- 4. WORK LOGS: Add work_units and work_cost columns mapped in WorkLog entity
ALTER TABLE work_logs ADD COLUMN IF NOT EXISTS work_units NUMERIC(10,2);
ALTER TABLE work_logs ADD COLUMN IF NOT EXISTS work_cost NUMERIC(10,2);

-- 5. USER API TOKENS: Create table for API tokens
CREATE TABLE IF NOT EXISTS user_api_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    token_key VARCHAR(255) NOT NULL UNIQUE,
    scope VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_api_tokens_user') THEN
        ALTER TABLE user_api_tokens ADD CONSTRAINT fk_user_api_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_user_api_tokens_user_id ON user_api_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_user_api_tokens_token_key ON user_api_tokens(token_key);

-- 6. MOBILE NOTIFICATION PREFERENCES: Create table for mobile notification preferences
CREATE TABLE IF NOT EXISTS mobile_notification_preferences (
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

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_mobile_notif_pref_user') THEN
        ALTER TABLE mobile_notification_preferences ADD CONSTRAINT fk_mobile_notif_pref_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_mobile_notif_pref_user_id ON mobile_notification_preferences(user_id);
