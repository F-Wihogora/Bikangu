-- V4__add_version_to_audit_logs.sql
-- Add version column to audit_logs table for optimistic locking

ALTER TABLE audit_logs ADD COLUMN version BIGINT DEFAULT 0;

-- Make version NOT NULL
ALTER TABLE audit_logs ALTER COLUMN version SET NOT NULL;