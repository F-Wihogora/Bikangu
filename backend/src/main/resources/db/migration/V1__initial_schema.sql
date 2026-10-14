-- V1__initial_schema.sql
-- Core tables for Bikangu FinTech Platform

-- Users and Authentication
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(20),
    nationality VARCHAR(100),
    id_document_type VARCHAR(50),
    id_document_number VARCHAR(100),
    id_document_expiry DATE,
    kyc_status VARCHAR(30) DEFAULT 'PENDING',
    kyc_verified_at TIMESTAMP,
    kyc_rejected_reason TEXT,
    is_active BOOLEAN DEFAULT true,
    is_email_verified BOOLEAN DEFAULT false,
    is_phone_verified BOOLEAN DEFAULT false,
    email_verified_at TIMESTAMP,
    phone_verified_at TIMESTAMP,
    last_login_at TIMESTAMP,
    failed_login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP,
    preferred_language VARCHAR(10) DEFAULT 'en',
    preferred_currency VARCHAR(3) DEFAULT 'RWF',
    timezone VARCHAR(50) DEFAULT 'Africa/Kigali',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone_number);
CREATE INDEX idx_users_kyc_status ON users(kyc_status);
CREATE INDEX idx_users_active ON users(is_active);

-- Roles and Permissions
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    is_system_role BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by UUID REFERENCES users(id),
    PRIMARY KEY (user_id, role_id)
);

-- Refresh Tokens
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    device_info VARCHAR(500),
    ip_address VARCHAR(45),
    user_agent TEXT,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    replaced_by_token_id UUID REFERENCES refresh_tokens(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);

-- User Sessions
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token_id UUID REFERENCES refresh_tokens(id) ON DELETE SET NULL,
    device_id VARCHAR(255),
    device_name VARCHAR(255),
    device_type VARCHAR(50),
    os VARCHAR(100),
    browser VARCHAR(100),
    ip_address VARCHAR(45),
    location VARCHAR(255),
    is_current BOOLEAN DEFAULT false,
    last_activity_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_sessions_user ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_device ON user_sessions(device_id);
CREATE INDEX idx_user_sessions_expires ON user_sessions(expires_at);

-- Financial Providers
CREATE TABLE financial_providers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(150) NOT NULL,
    provider_type VARCHAR(30) NOT NULL,
    category VARCHAR(30) NOT NULL,
    country_code VARCHAR(3) NOT NULL,
    currency_codes VARCHAR(10)[] NOT NULL,
    logo_url VARCHAR(500),
    icon_url VARCHAR(500),
    website_url VARCHAR(500),
    api_base_url VARCHAR(500),
    webhook_url VARCHAR(500),
    is_active BOOLEAN DEFAULT true,
    is_sandbox BOOLEAN DEFAULT false,
    supports_account_verification BOOLEAN DEFAULT true,
    supports_balance_check BOOLEAN DEFAULT true,
    supports_transaction_history BOOLEAN DEFAULT true,
    supports_transfer BOOLEAN DEFAULT true,
    supports_webhooks BOOLEAN DEFAULT true,
    rate_limit_per_minute INT DEFAULT 60,
    config_schema JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_providers_type ON financial_providers(provider_type);
CREATE INDEX idx_providers_country ON financial_providers(country_code);
CREATE INDEX idx_providers_active ON financial_providers(is_active);

-- Financial Accounts
CREATE TABLE financial_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider_id UUID NOT NULL REFERENCES financial_providers(id) ON DELETE RESTRICT,
    provider_account_id VARCHAR(255) NOT NULL,
    provider_account_type VARCHAR(50),
    account_name VARCHAR(255) NOT NULL,
    account_number_masked VARCHAR(50),
    account_holder_name VARCHAR(255),
    currency VARCHAR(3) NOT NULL DEFAULT 'RWF',
    is_primary BOOLEAN DEFAULT false,
    is_verified BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    last_sync_at TIMESTAMP,
    last_balance_check_at TIMESTAMP,
    current_balance NUMERIC(19, 4) DEFAULT 0,
    available_balance NUMERIC(19, 4) DEFAULT 0,
    verification_status VARCHAR(30) DEFAULT 'PENDING',
    verification_attempts INT DEFAULT 0,
    verified_at TIMESTAMP,
    verification_failed_reason TEXT,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, provider_id, provider_account_id)
);

CREATE INDEX idx_fin_accounts_user ON financial_accounts(user_id);
CREATE INDEX idx_fin_accounts_provider ON financial_accounts(provider_id);
CREATE INDEX idx_fin_accounts_primary ON financial_accounts(user_id, is_primary) WHERE is_primary = true;

-- Savings Vaults
CREATE TABLE savings_vaults (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    target_amount NUMERIC(19, 4) NOT NULL,
    current_amount NUMERIC(19, 4) DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'RWF',
    icon VARCHAR(100),
    color VARCHAR(20),
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    lock_date DATE,
    unlock_date DATE,
    status VARCHAR(30) DEFAULT 'ACTIVE',
    interest_rate NUMERIC(5, 4) DEFAULT 0,
    interest_calculated_at TIMESTAMP,
    auto_lock BOOLEAN DEFAULT true,
    allow_early_withdrawal BOOLEAN DEFAULT false,
    early_withdrawal_penalty_rate NUMERIC(5, 4) DEFAULT 0,
    minimum_balance NUMERIC(19, 4) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vaults_user ON savings_vaults(user_id);
CREATE INDEX idx_vaults_status ON savings_vaults(status);
CREATE INDEX idx_vaults_unlock_date ON savings_vaults(unlock_date);

-- Savings Goals
CREATE TABLE savings_goals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vault_id UUID REFERENCES savings_vaults(id) ON DELETE SET NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    target_amount NUMERIC(19, 4) NOT NULL,
    current_amount NUMERIC(19, 4) DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'RWF',
    icon VARCHAR(100),
    color VARCHAR(20),
    deadline DATE,
    lock_date DATE,
    status VARCHAR(30) DEFAULT 'ACTIVE',
    priority INT DEFAULT 0,
    auto_contribute BOOLEAN DEFAULT false,
    contribution_frequency VARCHAR(20),
    contribution_amount NUMERIC(19, 4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_goals_user ON savings_goals(user_id);
CREATE INDEX idx_goals_vault ON savings_goals(vault_id);
CREATE INDEX idx_goals_status ON savings_goals(status);
CREATE INDEX idx_goals_deadline ON savings_goals(deadline);

-- Savings Rules
CREATE TABLE savings_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vault_id UUID NOT NULL REFERENCES savings_vaults(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    rule_type VARCHAR(30) NOT NULL,
    percentage NUMERIC(5, 2),
    fixed_amount NUMERIC(19, 4),
    min_income_threshold NUMERIC(19, 4),
    max_income_threshold NUMERIC(19, 4),
    income_source VARCHAR(100),
    income_sources VARCHAR(100)[],
    priority INT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    is_paused BOOLEAN DEFAULT false,
    paused_at TIMESTAMP,
    pause_reason TEXT,
    execution_count INT DEFAULT 0,
    last_executed_at TIMESTAMP,
    total_saved NUMERIC(19, 4) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_rule_type CHECK (rule_type IN ('PERCENTAGE', 'FIXED_AMOUNT', 'CONDITIONAL_PERCENTAGE', 'INCOME_SOURCE', 'BONUS')),
    CONSTRAINT chk_percentage CHECK (percentage IS NULL OR (percentage >= 0 AND percentage <= 100)),
    CONSTRAINT chk_fixed_amount CHECK (fixed_amount IS NULL OR fixed_amount >= 0)
);

CREATE INDEX idx_savings_rules_user ON savings_rules(user_id);
CREATE INDEX idx_savings_rules_vault ON savings_rules(vault_id);
CREATE INDEX idx_savings_rules_active ON savings_rules(is_active, is_paused);

-- Transactions
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    financial_account_id UUID REFERENCES financial_accounts(id) ON DELETE SET NULL,
    vault_id UUID REFERENCES savings_vaults(id) ON DELETE SET NULL,
    goal_id UUID REFERENCES savings_goals(id) ON DELETE SET NULL,
    provider_id UUID REFERENCES financial_providers(id) ON DELETE SET NULL,
    external_transaction_id VARCHAR(255),
    idempotency_key VARCHAR(255) UNIQUE,
    transaction_type VARCHAR(30) NOT NULL,
    transaction_category VARCHAR(30),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'RWF',
    exchange_rate NUMERIC(10, 6) DEFAULT 1,
    base_amount NUMERIC(19, 4),
    base_currency VARCHAR(3),
    fee_amount NUMERIC(19, 4) DEFAULT 0,
    net_amount NUMERIC(19, 4),
    description TEXT,
    reference VARCHAR(255),
    provider_reference VARCHAR(255),
    income_source VARCHAR(100),
    income_category VARCHAR(100),
    metadata JSONB,
    processed_at TIMESTAMP,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_user ON transactions(user_id);
CREATE INDEX idx_transactions_account ON transactions(financial_account_id);
CREATE INDEX idx_transactions_vault ON transactions(vault_id);
CREATE INDEX idx_transactions_goal ON transactions(goal_id);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created ON transactions(created_at DESC);
CREATE INDEX idx_transactions_idempotency ON transactions(idempotency_key);
CREATE INDEX idx_transactions_external ON transactions(external_transaction_id);

-- Income Transactions (for tracking qualifying income)
CREATE TABLE income_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL UNIQUE REFERENCES transactions(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    financial_account_id UUID NOT NULL REFERENCES financial_accounts(id) ON DELETE CASCADE,
    provider_id UUID NOT NULL REFERENCES financial_providers(id) ON DELETE CASCADE,
    external_transaction_id VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'RWF',
    income_source VARCHAR(100) NOT NULL,
    income_category VARCHAR(100),
    received_at TIMESTAMP NOT NULL,
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_qualified BOOLEAN DEFAULT true,
    qualification_reason TEXT,
    processed BOOLEAN DEFAULT false,
    processed_at TIMESTAMP,
    savings_calculated NUMERIC(19, 4) DEFAULT 0,
    metadata JSONB
);

CREATE INDEX idx_income_transactions_user ON income_transactions(user_id);
CREATE INDEX idx_income_transactions_account ON income_transactions(financial_account_id);
CREATE INDEX idx_income_transactions_received ON income_transactions(received_at DESC);
CREATE INDEX idx_income_transactions_processed ON income_transactions(processed);
CREATE INDEX idx_income_transactions_external ON income_transactions(external_transaction_id);

-- Savings Transactions (for tracking executed savings)
CREATE TABLE savings_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL UNIQUE REFERENCES transactions(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vault_id UUID NOT NULL REFERENCES savings_vaults(id) ON DELETE CASCADE,
    rule_id UUID REFERENCES savings_rules(id) ON DELETE SET NULL,
    goal_id UUID REFERENCES savings_goals(id) ON DELETE SET NULL,
    financial_account_id UUID NOT NULL REFERENCES financial_accounts(id) ON DELETE CASCADE,
    provider_id UUID NOT NULL REFERENCES financial_providers(id) ON DELETE CASCADE,
    external_transfer_id VARCHAR(255),
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'RWF',
    fee_amount NUMERIC(19, 4) DEFAULT 0,
    net_amount NUMERIC(19, 4),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    transfer_status VARCHAR(30),
    initiated_at TIMESTAMP,
    completed_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    receipt_generated BOOLEAN DEFAULT false,
    receipt_url VARCHAR(500),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_savings_transactions_user ON savings_transactions(user_id);
CREATE INDEX idx_savings_transactions_vault ON savings_transactions(vault_id);
CREATE INDEX idx_savings_transactions_rule ON savings_transactions(rule_id);
CREATE INDEX idx_savings_transactions_status ON savings_transactions(status);
CREATE INDEX idx_savings_transactions_created ON savings_transactions(created_at DESC);

-- Notifications
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    category VARCHAR(30) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    priority VARCHAR(20) DEFAULT 'NORMAL',
    channels VARCHAR(20)[] DEFAULT ARRAY['IN_APP'],
    data JSONB,
    action_url VARCHAR(500),
    action_label VARCHAR(100),
    is_read BOOLEAN DEFAULT false,
    read_at TIMESTAMP,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason TEXT,
    fcm_message_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(user_id, is_read);
CREATE INDEX idx_notifications_created ON notifications(created_at DESC);
CREATE INDEX idx_notifications_type ON notifications(type);

-- Notification Preferences
CREATE TABLE notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    email_enabled BOOLEAN DEFAULT true,
    push_enabled BOOLEAN DEFAULT true,
    in_app_enabled BOOLEAN DEFAULT true,
    sms_enabled BOOLEAN DEFAULT false,
    salary_received BOOLEAN DEFAULT true,
    savings_executed BOOLEAN DEFAULT true,
    savings_failed BOOLEAN DEFAULT true,
    goal_achieved BOOLEAN DEFAULT true,
    goal_nearing_deadline BOOLEAN DEFAULT true,
    account_connected BOOLEAN DEFAULT true,
    account_disconnected BOOLEAN DEFAULT true,
    security_alerts BOOLEAN DEFAULT true,
    failed_login BOOLEAN DEFAULT true,
    password_changed BOOLEAN DEFAULT true,
    pin_changed BOOLEAN DEFAULT true,
    withdrawal_requested BOOLEAN DEFAULT true,
    transfer_completed BOOLEAN DEFAULT true,
    weekly_summary BOOLEAN DEFAULT false,
    monthly_report BOOLEAN DEFAULT true,
    marketing_emails BOOLEAN DEFAULT false,
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    timezone VARCHAR(50) DEFAULT 'Africa/Kigali',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Audit Logs
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    actor_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id UUID,
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    session_id UUID,
    correlation_id UUID,
    status VARCHAR(20) DEFAULT 'SUCCESS',
    error_message TEXT,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_id);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource_type, resource_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_logs_correlation ON audit_logs(correlation_id);

-- Security Events
CREATE TABLE security_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    event_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    description TEXT NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    device_info JSONB,
    location VARCHAR(255),
    metadata JSONB,
    resolved BOOLEAN DEFAULT false,
    resolved_at TIMESTAMP,
    resolved_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_security_events_user ON security_events(user_id);
CREATE INDEX idx_security_events_type ON security_events(event_type);
CREATE INDEX idx_security_events_severity ON security_events(severity);
CREATE INDEX idx_security_events_created ON security_events(created_at DESC);
CREATE INDEX idx_security_events_resolved ON security_events(resolved);

-- KYC Records
CREATE TABLE kyc_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(30) DEFAULT 'PENDING',
    tier VARCHAR(20) DEFAULT 'BASIC',
    document_type VARCHAR(50),
    document_number VARCHAR(100),
    document_front_url VARCHAR(500),
    document_back_url VARCHAR(500),
    selfie_url VARCHAR(500),
    verified_by UUID REFERENCES users(id),
    verified_at TIMESTAMP,
    rejected_reason TEXT,
    risk_score INT DEFAULT 0,
    risk_factors JSONB,
    aml_check_status VARCHAR(30),
    aml_check_at TIMESTAMP,
    aml_check_result JSONB,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_kyc_records_user ON kyc_records(user_id);
CREATE INDEX idx_kyc_records_status ON kyc_records(status);

-- Webhook Events
CREATE TABLE webhook_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id UUID NOT NULL REFERENCES financial_providers(id) ON DELETE CASCADE,
    event_type VARCHAR(100) NOT NULL,
    external_event_id VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    headers JSONB,
    signature VARCHAR(500),
    status VARCHAR(30) DEFAULT 'RECEIVED',
    processing_started_at TIMESTAMP,
    processing_completed_at TIMESTAMP,
    processing_error TEXT,
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 5,
    next_retry_at TIMESTAMP,
    idempotency_key VARCHAR(255) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_webhook_events_provider ON webhook_events(provider_id);
CREATE INDEX idx_webhook_events_status ON webhook_events(status);
CREATE INDEX idx_webhook_events_external ON webhook_events(external_event_id);
CREATE INDEX idx_webhook_events_idempotency ON webhook_events(idempotency_key);
CREATE INDEX idx_webhook_events_next_retry ON webhook_events(next_retry_at);