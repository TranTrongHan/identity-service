-- V5__add_google_oauth_and_reset_password.sql
-- Purpose: Add Google OAuth config to app table, create state table and reset_password_request table.

-- Google OAuth fields on app
ALTER TABLE app ADD COLUMN google_client_id VARCHAR(200);
ALTER TABLE app ADD COLUMN google_client_secret VARCHAR(200);
ALTER TABLE app ADD COLUMN google_allowed_domain VARCHAR(200);
ALTER TABLE app ADD COLUMN reset_password_url_template VARCHAR(500);

-- Google OAuth state (CSRF protection, short-lived)
CREATE TABLE IF NOT EXISTS app_google_state (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    app_id UUID NOT NULL,
    state UUID NOT NULL,
    redirect_uri TEXT NOT NULL,
    expired_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_google_state_app FOREIGN KEY (app_id) REFERENCES app(id)
);
CREATE INDEX idx_google_state_lookup ON app_google_state(app_id, state);

-- Reset password request (one-time use, expires after N minutes)
CREATE TABLE IF NOT EXISTS reset_password_request (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    app_id UUID NOT NULL,
    account_id UUID NOT NULL,
    expired_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_reset_pw_app FOREIGN KEY (app_id) REFERENCES app(id),
    CONSTRAINT fk_reset_pw_account FOREIGN KEY (account_id) REFERENCES account(id)
);
CREATE INDEX idx_reset_pw_expired ON reset_password_request(expired_at);
