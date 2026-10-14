-- V8__add_version_to_all_tables.sql
-- Add version column to all tables that extend BaseEntity for optimistic locking (idempotent)

-- Users
ALTER TABLE users ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE users ALTER COLUMN version SET NOT NULL;

-- Roles
ALTER TABLE roles ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE roles ALTER COLUMN version SET NOT NULL;

-- Permissions
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE permissions ALTER COLUMN version SET NOT NULL;

-- Refresh Tokens
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE refresh_tokens ALTER COLUMN version SET NOT NULL;

-- User Sessions
ALTER TABLE user_sessions ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE user_sessions ALTER COLUMN version SET NOT NULL;

-- Financial Providers
ALTER TABLE financial_providers ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE financial_providers ALTER COLUMN version SET NOT NULL;

-- Financial Accounts
ALTER TABLE financial_accounts ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE financial_accounts ALTER COLUMN version SET NOT NULL;

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

-- Audit Logs
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE audit_logs ALTER COLUMN version SET NOT NULL;

-- Security Events
ALTER TABLE security_events ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE security_events ALTER COLUMN version SET NOT NULL;

-- KYC Records
ALTER TABLE kyc_records ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE kyc_records ALTER COLUMN version SET NOT NULL;

-- Webhook Events
ALTER TABLE webhook_events ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE webhook_events ALTER COLUMN version SET NOT NULL;

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

-- Financial Providers
ALTER TABLE financial_providers ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
ALTER TABLE financial_providers ALTER COLUMN version SET NOT NULL;

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