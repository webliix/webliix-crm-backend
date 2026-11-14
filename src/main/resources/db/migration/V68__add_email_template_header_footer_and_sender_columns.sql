-- V68: Add header_html, footer_html, and sender_email columns to email_templates table

ALTER TABLE email_templates ADD COLUMN IF NOT EXISTS sender_email VARCHAR(120) DEFAULT 'noreply@webliix.com';
ALTER TABLE email_templates ADD COLUMN IF NOT EXISTS header_html TEXT;
ALTER TABLE email_templates ADD COLUMN IF NOT EXISTS footer_html TEXT;

-- Backfill existing rows with default sender email if null
UPDATE email_templates SET sender_email = 'noreply@webliix.com' WHERE sender_email IS NULL;
