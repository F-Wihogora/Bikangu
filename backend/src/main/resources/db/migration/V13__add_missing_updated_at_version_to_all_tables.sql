-- V13__add_missing_updated_at_version_to_all_tables.sql
-- Add missing updated_at and version columns to all tables that extend BaseEntity

-- Refresh Tokens
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE refresh_tokens ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE refresh_tokens ALTER COLUMN version SET NOT NULL;
UPDATE refresh_tokens SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
UPDATE refresh_tokens SET version = 0 WHERE version IS NULL;
ALTER TABLE refresh_tokens ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE refresh_tokens ALTER COLUMN version SET NOT NULL;

-- Security Events
ALTER TABLE security_events ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE security_events ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE security_events ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE security_events ALTER COLUMN version SET NOT NULL;
UPDATE security_events SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
UPDATE security_events SET version = 0 WHERE version IS NULL;
ALTER TABLE security_events ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE security_events ALTER COLUMN version SET NOT NULL;

-- User Sessions
ALTER TABLE user_sessions ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE user_sessions ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE user_sessions ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE user_sessions ALTER COLUMN version SET NOT NULL;
UPDATE user_sessions SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
UPDATE user_sessions SET version = 0 WHERE version IS NULL;
ALTER TABLE user_sessions ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE user_sessions ALTER COLUMN version SET NOT NULL;

-- User Roles
ALTER TABLE user_roles ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE user_roles ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE user_roles ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE user_roles ALTER COLUMN version SET NOT NULL;
UPDATE user_roles SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
UPDATE user_roles SET version = 0 WHERE version IS NULL;
ALTER TABLE user_roles ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE user_roles ALTER COLUMN version SET NOT NULL;

-- Role Permissions
ALTER TABLE role_permissions ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE role_permissions ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE role_permissions ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE role_permissions ALTER COLUMN version SET NOT NULL;
UPDATE role_permissions SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
UPDATE role_permissions SET version = 0 WHERE version IS NULL;
ALTER TABLE role_permissions ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE role_permissions ALTER COLUMN version SET NOT NULL;

-- KYC Records
ALTER TABLE kyc_records ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE kyc_records ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE kyc_records ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE kyc_records ALTER COLUMN version SET NOT NULL;
UPDATE kyc_records SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
UPDATE kyc_records SET version = 0 WHERE version IS NULL;
ALTER TABLE kyc_records ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE kyc_records ALTER COLUMN version SET NOT NULL;

-- Webhook Events
ALTER TABLE webhook_events ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE webhook_events ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE webhook_events ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE webhook_events ALTER COLUMN version SET NOT NULL;
UPDATE webhook_events SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
UPDATE webhook_events SET version = 0 WHERE version IS NULL;
ALTER TABLE webhook_events ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE webhook_events ALTER COLUMN version SET NOT NULL;

-- Notification Preferences
ALTER TABLE notification_preferences ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE notification_preferences ALTER COLUMN version SET NOT NULL;
UPDATE notification_preferences SET version = 0 WHERE version IS NULL;
ALTER TABLE notification_preferences ALTER COLUMN version SET NOT NULL;

-- Savings Vaults
ALTER TABLE savings_vaults ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE savings_vaults ALTER COLUMN version SET NOT NULL;

-- Savings Goals
ALTER TABLE savings_goals ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE savings_goals ALTER COLUMN version SET NOT NULL;

-- Savings Rules
ALTER TABLE savings_rules ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE savings_rules ALTER COLUMN version SET NOT NULL;

-- Transactions
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE transactions ALTER COLUMN version SET NOT NULL;

-- Income Transactions
ALTER TABLE income_transactions ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE income_transactions ALTER COLUMN version SET NOT NULL;

-- Savings Transactions
ALTER TABLE savings_transactions ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE savings_transactions ALTER COLUMN version SET NOT NULL;

-- Security Events
ALTER TABLE security_events ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE security_events ALTER COLUMN version SET NOT NULL;

-- Notifications
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE notifications ALTER COLUMN version SET NOT NULL;

-- KYC Records
ALTER TABLE kyc_records ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE kyc_records ALTER COLUMN version SET NOT NULL;

-- Webhook Events
ALTER TABLE webhook_events ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE webhook_events ALTER COLUMN version SET NOT NULL;