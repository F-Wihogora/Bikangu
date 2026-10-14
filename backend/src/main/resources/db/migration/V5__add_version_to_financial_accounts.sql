-- V5__add_version_to_financial_accounts.sql
-- Add version column to financial_accounts table for optimistic locking

ALTER TABLE financial_accounts ADD COLUMN version BIGINT DEFAULT 0;

-- Make version NOT NULL
ALTER TABLE financial_accounts ALTER COLUMN version SET NOT NULL;