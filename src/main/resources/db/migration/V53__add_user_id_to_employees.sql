-- Add user_id FK to employees table (nullable for existing rows)
ALTER TABLE employees ADD COLUMN IF NOT EXISTS user_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_employees_user'
    ) THEN
        ALTER TABLE employees
            ADD CONSTRAINT fk_employees_user
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_employees_user_id ON employees(user_id) WHERE user_id IS NOT NULL;
