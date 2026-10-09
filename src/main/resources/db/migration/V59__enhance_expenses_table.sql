-- Enhance expenses table with title, status, notes, reference_number, receipt_url, and updated_at
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS title VARCHAR(255);
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'APPROVED';
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS reference_number VARCHAR(255);
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS receipt_url VARCHAR(500);
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Populate default title for existing rows if any
UPDATE expenses SET title = description WHERE title IS NULL AND description IS NOT NULL;
UPDATE expenses SET status = 'APPROVED' WHERE status IS NULL;

CREATE INDEX IF NOT EXISTS idx_expenses_category ON expenses(category);
CREATE INDEX IF NOT EXISTS idx_expenses_status ON expenses(status);
CREATE INDEX IF NOT EXISTS idx_expenses_expense_date ON expenses(expense_date);
