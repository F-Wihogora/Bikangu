-- V11__add_missing_updated_at_to_permissions.sql
-- Add missing updated_at column to permissions table

ALTER TABLE permissions ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE permissions ALTER COLUMN updated_at SET NOT NULL;

UPDATE permissions SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;

ALTER TABLE permissions ALTER COLUMN updated_at SET NOT NULL;