-- V10__add_missing_columns_to_notifications.sql
-- Add missing updated_at column to notifications table

-- Add updated_at column
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE notifications ALTER COLUMN updated_at SET NOT NULL;

-- Update existing records
UPDATE notifications SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;

-- Make column NOT NULL
ALTER TABLE notifications ALTER COLUMN updated_at SET NOT NULL;