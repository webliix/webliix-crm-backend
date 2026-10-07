-- Add work_units and work_cost to work_logs table
ALTER TABLE work_logs ADD COLUMN IF NOT EXISTS work_units NUMERIC(10,2);
ALTER TABLE work_logs ADD COLUMN IF NOT EXISTS work_cost NUMERIC(19,2) DEFAULT 0;
