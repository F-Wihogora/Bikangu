-- V14__add_missing_columns_to_transactions.sql
-- Add missing initiated_at column to transactions table

ALTER TABLE transactions ADD COLUMN IF NOT EXISTS initiated_at TIMESTAMP;
ALTER TABLE transactions ALTER COLUMN initiated_at SET NOT NULL;

UPDATE transactions SET initiated_at = created_at WHERE initiated_at IS NULL;

ALTER TABLE transactions ALTER COLUMN initiated_at SET NOT NULL;