-- V2__seed_data.sql
-- Default roles, permissions, and providers

-- Insert default roles
INSERT INTO roles (name, description, is_system_role) VALUES
('SUPER_ADMIN', 'Super administrator with full system access', true),
('ADMIN', 'Administrator with management access', true),
('SUPPORT', 'Customer support with limited access', true),
('COMPLIANCE', 'Compliance officer for KYC/AML review', true),
('USER', 'Regular application user', true);

-- Insert default permissions
INSERT INTO permissions (name, description, resource, action) VALUES
-- User management
('user:read', 'View user profiles', 'user', 'read'),
('user:write', 'Create and update users', 'user', 'write'),
('user:delete', 'Delete users', 'user', 'delete'),
('user:list', 'List all users', 'user', 'list'),
('user:suspend', 'Suspend/activate users', 'user', 'suspend'),
('user:kyc', 'Manage KYC status', 'user', 'kyc'),

-- Financial accounts
('account:read', 'View financial accounts', 'account', 'read'),
('account:write', 'Link/unlink financial accounts', 'account', 'write'),
('account:verify', 'Verify financial accounts', 'account', 'verify'),

-- Savings
('savings:rule:read', 'View savings rules', 'savings:rule', 'read'),
('savings:rule:write', 'Create and update savings rules', 'savings:rule', 'write'),
('savings:rule:delete', 'Delete savings rules', 'savings:rule', 'delete'),
('savings:vault:read', 'View savings vaults', 'savings:vault', 'read'),
('savings:vault:write', 'Create and update savings vaults', 'savings:vault', 'write'),
('savings:vault:withdraw', 'Withdraw from savings vaults', 'savings:vault', 'withdraw'),
('savings:goal:read', 'View savings goals', 'savings:goal', 'read'),
('savings:goal:write', 'Create and update savings goals', 'savings:goal', 'write'),
('savings:transaction:read', 'View savings transactions', 'savings:transaction', 'read'),

-- Transactions
('transaction:read', 'View transactions', 'transaction', 'read'),
('transaction:export', 'Export transactions', 'transaction', 'export'),

-- Providers
('provider:read', 'View financial providers', 'provider', 'read'),
('provider:write', 'Manage financial providers', 'provider', 'write'),
('provider:webhook', 'Process provider webhooks', 'provider', 'webhook'),

-- Analytics
('analytics:read', 'View analytics', 'analytics', 'read'),
('analytics:export', 'Export analytics', 'analytics', 'export'),

-- Notifications
('notification:read', 'View notifications', 'notification', 'read'),
('notification:write', 'Send notifications', 'notification', 'write'),
('notification:preferences', 'Manage notification preferences', 'notification', 'preferences'),

-- Admin
('admin:dashboard', 'Access admin dashboard', 'admin', 'dashboard'),
('admin:settings', 'Manage system settings', 'admin', 'settings'),
('admin:audit', 'View audit logs', 'admin', 'audit'),
('admin:security', 'View security events', 'admin', 'security');

-- Assign permissions to roles
-- SUPER_ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'SUPER_ADMIN';

-- ADMIN gets most permissions except user:delete and admin:settings
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN' AND p.name NOT IN ('user:delete', 'admin:settings');

-- SUPPORT gets read permissions and limited write
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'SUPPORT' AND p.name IN (
    'user:read', 'user:list', 'user:suspend',
    'account:read', 'account:verify',
    'savings:rule:read', 'savings:vault:read', 'savings:goal:read', 'savings:transaction:read',
    'transaction:read', 'transaction:export',
    'notification:read', 'notification:write',
    'admin:dashboard'
);

-- COMPLIANCE gets KYC and audit permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'COMPLIANCE' AND p.name IN (
    'user:read', 'user:list', 'user:kyc',
    'account:read',
    'savings:rule:read', 'savings:vault:read', 'savings:goal:read', 'savings:transaction:read',
    'transaction:read', 'transaction:export',
    'admin:dashboard', 'admin:audit', 'admin:security'
);

-- USER gets basic permissions for their own data
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'USER' AND p.name IN (
    'account:read', 'account:write',
    'savings:rule:read', 'savings:rule:write', 'savings:rule:delete',
    'savings:vault:read', 'savings:vault:write', 'savings:vault:withdraw',
    'savings:goal:read', 'savings:goal:write',
    'savings:transaction:read',
    'transaction:read', 'transaction:export',
    'notification:read', 'notification:preferences'
);

-- Insert default financial providers for Rwanda
INSERT INTO financial_providers (name, display_name, provider_type, category, country_code, currency_codes, logo_url, icon_url, website_url, is_active, is_sandbox, supports_account_verification, supports_balance_check, supports_transaction_history, supports_transfer, supports_webhooks, rate_limit_per_minute, config_schema) VALUES
('MTN_MOMO', 'MTN Mobile Money', 'MOBILE_MONEY', 'TELCO', 'RW', ARRAY['RWF'], 'https://assets.bikangu.com/providers/mtn-momo-logo.png', 'https://assets.bikangu.com/providers/mtn-momo-icon.png', 'https://www.mtn.rw', true, false, true, true, true, true, true, 60, '{"apiVersion": "v1", "environment": "production", "requiredFields": ["phoneNumber", "pin"]}'),
('AIRTEL_MONEY', 'Airtel Money', 'MOBILE_MONEY', 'TELCO', 'RW', ARRAY['RWF'], 'https://assets.bikangu.com/providers/airtel-money-logo.png', 'https://assets.bikangu.com/providers/airtel-money-icon.png', 'https://www.airtel.rw', true, false, true, true, true, true, true, 60, '{"apiVersion": "v1", "environment": "production", "requiredFields": ["phoneNumber", "pin"]}'),
('BK_BANK', 'Bank of Kigali', 'BANK', 'COMMERCIAL_BANK', 'RW', ARRAY['RWF'], 'https://assets.bikangu.com/providers/bk-logo.png', 'https://assets.bikangu.com/providers/bk-icon.png', 'https://www.bk.rw', true, false, true, true, true, true, true, 30, '{"apiVersion": "v1", "environment": "production", "requiredFields": ["accountNumber", "pin"]}'),
('EQUITY_BANK', 'Equity Bank Rwanda', 'BANK', 'COMMERCIAL_BANK', 'RW', ARRAY['RWF'], 'https://assets.bikangu.com/providers/equity-logo.png', 'https://assets.bikangu.com/providers/equity-icon.png', 'https://rw.equitybankgroup.com', true, false, true, true, true, true, true, 30, '{"apiVersion": "v1", "environment": "production", "requiredFields": ["accountNumber", "pin"]}'),
('I_AND_M_BANK', 'I&M Bank Rwanda', 'BANK', 'COMMERCIAL_BANK', 'RW', ARRAY['RWF'], 'https://assets.bikangu.com/providers/im-logo.png', 'https://assets.bikangu.com/providers/im-icon.png', 'https://www.imbankgroup.com/rw', true, false, true, true, true, true, true, 30, '{"apiVersion": "v1", "environment": "production", "requiredFields": ["accountNumber", "pin"]}'),
('COGEBANK', 'COGEBANK', 'BANK', 'COMMERCIAL_BANK', 'RW', ARRAY['RWF'], 'https://assets.bikangu.com/providers/cogebank-logo.png', 'https://assets.bikangu.com/providers/cogebank-icon.png', 'https://www.cogebank.rw', true, false, true, true, true, true, true, 30, '{"apiVersion": "v1", "environment": "production", "requiredFields": ["accountNumber", "pin"]}'),
('AB_BANK', 'AB Bank Rwanda', 'BANK', 'COMMERCIAL_BANK', 'RW', ARRAY['RWF'], 'https://assets.bikangu.com/providers/ab-logo.png', 'https://assets.bikangu.com/providers/ab-icon.png', 'https://www.abbank.rw', true, false, true, true, true, true, true, 30, '{"apiVersion": "v1", "environment": "production", "requiredFields": ["accountNumber", "pin"]}'),
('MOCK_PROVIDER', 'Mock Financial Provider (Development)', 'MOCK', 'DEVELOPMENT', 'RW', ARRAY['RWF', 'USD', 'EUR'], 'https://assets.bikangu.com/providers/mock-logo.png', 'https://assets.bikangu.com/providers/mock-icon.png', 'http://localhost:8081', true, true, true, true, true, true, true, 1000, '{"apiVersion": "v1", "environment": "development", "baseUrl": "http://localhost:8081", "requiredFields": []}');

-- Create default notification preferences for new users (handled in application code)

-- Insert system settings
-- (Settings table would be created in a later migration if needed)