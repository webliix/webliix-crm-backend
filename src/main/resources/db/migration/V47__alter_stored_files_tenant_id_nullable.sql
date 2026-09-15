-- Make tenant_id nullable and default to 1 in stored_files table
ALTER TABLE stored_files ALTER COLUMN tenant_id DROP NOT NULL;
ALTER TABLE stored_files ALTER COLUMN tenant_id SET DEFAULT 1;
