-- V9__add_missing_columns_to_income_transactions.sql
-- Add missing created_at, updated_at, version columns to income_transactions table

-- Add created_at column
ALTER TABLE income_transactions ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE income_transactions ALTER COLUMN created_at SET NOT NULL;

-- Add updated_at column
ALTER TABLE income_transactions ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE income_transactions ALTER COLUMN updated_at SET NOT NULL;

-- Add version column for optimistic locking
ALTER TABLE income_transactions ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE income_transactions ALTER COLUMN version SET NOT NULL;

-- Update existing records
UPDATE income_transactions SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
UPDATE income_transactions SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
UPDATE income_transactions SET version = 0 WHERE version IS NULL;

-- Make columns NOT NULL
ALTER TABLE income_transactions ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE income_transactions ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE income_transactions ALTER COLUMN version SET NOT NULL;