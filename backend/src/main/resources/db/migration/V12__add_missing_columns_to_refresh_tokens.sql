-- V12__add_missing_columns_to_refresh_tokens.sql
-- Add missing updated_at and version columns to refresh_tokens table

ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE refresh_tokens ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE refresh_tokens ALTER COLUMN version SET NOT NULL;

UPDATE refresh_tokens SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
UPDATE refresh_tokens SET version = 0 WHERE version IS NULL;

ALTER TABLE refresh_tokens ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE refresh_tokens ALTER COLUMN version SET NOT NULL;