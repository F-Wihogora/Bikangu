-- V7__add_version_to_financial_providers.sql
-- Add version column to financial_providers table for optimistic locking

ALTER TABLE financial_providers ADD COLUMN version BIGINT DEFAULT 0;

-- Make version NOT NULL
ALTER TABLE financial_providers ALTER COLUMN version SET NOT NULL;