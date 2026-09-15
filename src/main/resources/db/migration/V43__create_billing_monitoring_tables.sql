-- Phase 21: SaaS Billing, Email Templates, Monitoring

CREATE TABLE IF NOT EXISTS plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    price_per_month DECIMAL(10,2),
    price_per_year DECIMAL(10,2),
    max_users INT,
    max_storage_gb INT,
    max_api_calls_per_month BIGINT,
    features TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    plan_id BIGINT,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    started_at TIMESTAMP,
    expires_at TIMESTAMP,
    next_billing_date TIMESTAMP,
    auto_renew BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_subscription_plan
        FOREIGN KEY (plan_id) REFERENCES plans(id)
);

CREATE TABLE IF NOT EXISTS email_templates (
    id BIGSERIAL PRIMARY KEY,
    template_key VARCHAR(255) NOT NULL UNIQUE,
    subject VARCHAR(255),
    html_content TEXT NOT NULL,
    plain_text_content TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    version INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tenant_metrics (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    active_users BIGINT DEFAULT 0,
    storage_used_gb DOUBLE PRECISION DEFAULT 0.0,
    api_calls_this_month BIGINT DEFAULT 0,
    ai_usage_tokens BIGINT DEFAULT 0,
    email_sent_count BIGINT DEFAULT 0,
    recorded_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Trigger function for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- plans trigger
CREATE TRIGGER trg_plans_updated_at
BEFORE UPDATE ON plans
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- subscriptions trigger
CREATE TRIGGER trg_subscriptions_updated_at
BEFORE UPDATE ON subscriptions
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- email_templates trigger
CREATE TRIGGER trg_email_templates_updated_at
BEFORE UPDATE ON email_templates
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();