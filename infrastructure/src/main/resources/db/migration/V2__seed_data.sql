-- V2__seed_data.sql
-- Purpose: Seed root account, IDENTITY app, and admin access for bootstrap

-- Insert the IDENTITY app (this service itself)
INSERT INTO app (id, code, name, description, signing_key, token_lifetime_minutes, session_lifetime_minutes)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'IDENTITY',
    'Identity Service',
    'The identity and authorization management service',
    'CHANGE_ME_TO_A_SECURE_128_CHAR_KEY_0000000000000000000000000000000000000000000000000000000000000000000000000000000000',
    15,
    1440
);

-- Insert root account (password: "root" hashed with BCrypt)
-- BCrypt hash of "root" with default strength
INSERT INTO account (id, name, secret_key, password)
VALUES (
    'b0000000-0000-0000-0000-000000000001',
    'Root Administrator',
    'root-secret-key-placeholder',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
);

-- Insert root account auth (username login method)
INSERT INTO account_auth (account_id, field_type, field_value)
VALUES (
    'b0000000-0000-0000-0000-000000000001',
    1,
    'root'
);

-- Grant root account admin access to IDENTITY app
INSERT INTO app_access (account_id, app_id, scope)
VALUES (
    'b0000000-0000-0000-0000-000000000001',
    'a0000000-0000-0000-0000-000000000001',
    'admin'
);
