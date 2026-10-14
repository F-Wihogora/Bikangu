-- V3__add_updated_at_to_audit_logs.sql
-- Add updated_at column to audit_logs table

ALTER TABLE audit_logs ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Update existing records to have updated_at = created_at
UPDATE audit_logs SET updated_at = created_at WHERE updated_at IS NULL;

-- Make updated_at NOT NULL
ALTER TABLE audit_logs ALTER COLUMN updated_at SET NOT NULL;