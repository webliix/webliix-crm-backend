-- V61: Enhance email templates and project management

-- 1. Enhance email_templates columns
ALTER TABLE email_templates ADD COLUMN IF NOT EXISTS description VARCHAR(500);
ALTER TABLE email_templates ADD COLUMN IF NOT EXISTS sample_variables TEXT;

-- 2. Add currency and billing_type to projects
ALTER TABLE projects ADD COLUMN IF NOT EXISTS currency VARCHAR(10) DEFAULT 'USD';
ALTER TABLE projects ADD COLUMN IF NOT EXISTS billing_type VARCHAR(50) DEFAULT 'FIXED';

-- 3. Default system notification settings
INSERT INTO system_settings (setting_key, setting_value, setting_group, description, data_type, editable, created_at, updated_at)
VALUES 
    ('notify.global.email_enabled', 'true', 'NOTIFICATIONS', 'Global kill switch for all outbound email dispatch', 'BOOLEAN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('notify.global.inapp_enabled', 'true', 'NOTIFICATIONS', 'Global kill switch for all in-app notifications', 'BOOLEAN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('notify.lead.created.email', 'true', 'NOTIFICATIONS', 'Send email when a new lead is created', 'BOOLEAN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('notify.lead.converted.email', 'true', 'NOTIFICATIONS', 'Send welcome email when lead is converted to client', 'BOOLEAN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('notify.invoice.created.email', 'true', 'NOTIFICATIONS', 'Send invoice issued email to client', 'BOOLEAN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('notify.invoice.paid.email', 'true', 'NOTIFICATIONS', 'Send payment receipt email when invoice is paid', 'BOOLEAN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('notify.project.created.email', 'true', 'NOTIFICATIONS', 'Send email notification when new project is initiated', 'BOOLEAN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('notify.ticket.created.email', 'true', 'NOTIFICATIONS', 'Send email when support ticket is opened', 'BOOLEAN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('notify.ticket.assigned.email', 'true', 'NOTIFICATIONS', 'Send email to staff when ticket is assigned', 'BOOLEAN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('notify.auth.otp.email', 'true', 'NOTIFICATIONS', 'Send email for OTP verification and password reset', 'BOOLEAN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('notify.worklog.submitted.email', 'true', 'NOTIFICATIONS', 'Send email to manager when work log is submitted', 'BOOLEAN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (setting_key) DO NOTHING;
