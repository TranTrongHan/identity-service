-- V3__add_indexes.sql
-- Purpose: Add indexes for frequently queried columns to improve performance

-- account_auth: lookup by field_type + field_value (login flow)
CREATE INDEX IF NOT EXISTS idx_account_auth_account_id ON account_auth(account_id);

-- app: lookup by code (JWT issuer resolution, login flow)
CREATE INDEX IF NOT EXISTS idx_app_code ON app(code);

-- app_permission: lookup by app_id (permission listing)
CREATE INDEX IF NOT EXISTS idx_app_permission_app_id ON app_permission(app_id);

-- app_role: lookup by app_id (role listing)
CREATE INDEX IF NOT EXISTS idx_app_role_app_id ON app_role(app_id);

-- app_role_permission: lookup by role_id (scope resolution)
CREATE INDEX IF NOT EXISTS idx_app_role_permission_role_id ON app_role_permission(role_id);

-- app_access: lookup by account_id + app_id (login flow)
CREATE INDEX IF NOT EXISTS idx_app_access_account_id ON app_access(account_id);
CREATE INDEX IF NOT EXISTS idx_app_access_app_id ON app_access(app_id);

-- account_session: lookup by account_id (session management)
CREATE INDEX IF NOT EXISTS idx_account_session_account_id ON account_session(account_id);
CREATE INDEX IF NOT EXISTS idx_account_session_app_id ON account_session(app_id);
CREATE INDEX IF NOT EXISTS idx_account_session_expired_at ON account_session(expired_at);
