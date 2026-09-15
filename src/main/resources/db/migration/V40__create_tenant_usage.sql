-- Recreated migration for tenant_metrics table as tenant usage metrics
CREATE TABLE IF NOT EXISTS tenant_metrics
(
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    active_users BIGINT DEFAULT 0,
    storage_used_gb DOUBLE PRECISION DEFAULT 0.0,
    api_calls_this_month BIGINT DEFAULT 0,
    ai_usage_tokens BIGINT DEFAULT 0,
    email_sent_count BIGINT DEFAULT 0,
    recorded_at TIMESTAMP,
    created_at TIMESTAMP,
    CONSTRAINT fk_tenant_metrics_tenant FOREIGN KEY(tenant_id) REFERENCES tenants(id)
);
