-- Normalize legacy or demo lead statuses and null values
UPDATE leads SET status = 'NEW' WHERE status = 'OPEN';
UPDATE leads SET status = 'NEW' WHERE status IS NULL;
UPDATE leads SET source = 'WEBSITE' WHERE source IS NULL;
UPDATE leads SET converted = FALSE WHERE converted IS NULL;
