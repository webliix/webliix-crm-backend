-- V65: Add transaction reference and fix invoice paid and pending amounts
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS transaction_reference VARCHAR(255);
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50);
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS paid_date DATE;

-- Synchronize any invoices where status is PAID but paid_amount was 0 or pending_amount was left positive
UPDATE invoices
SET paid_amount = total_amount,
    pending_amount = 0
WHERE status = 'PAID' AND (paid_amount IS NULL OR paid_amount = 0 OR pending_amount > 0);

-- Synchronize any pending, sent, or overdue invoices so pending_amount accurately reflects (total_amount - paid_amount)
UPDATE invoices
SET pending_amount = GREATEST(0, total_amount - COALESCE(paid_amount, 0))
WHERE status != 'PAID' AND (pending_amount IS NULL OR pending_amount = total_amount AND COALESCE(paid_amount, 0) > 0);
