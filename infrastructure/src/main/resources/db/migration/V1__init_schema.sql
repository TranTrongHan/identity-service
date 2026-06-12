-- V1__init_schema.sql
-- Purpose: Create all core tables for the identity service

CREATE TABLE IF NOT EXISTS app (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    signing_key VARCHAR(128) NOT NULL,
    token_lifetime_minutes INT NOT NULL DEFAULT 15,
    session_lifetime_minutes INT NOT NULL DEFAULT 1440,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS account (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    avatar_url TEXT,
    secret_key VARCHAR(100) NOT NULL,
    password VARCHAR(100),
    wrong_login_count INT NOT NULL DEFAULT 0,
    access_denied_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS account_auth (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    field_type INT NOT NULL,
    field_value VARCHAR(200) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_account_auth_account FOREIGN KEY (account_id) REFERENCES account(id),
    CONSTRAINT uq_account_auth_field_type_value UNIQUE (field_type, field_value)
);

CREATE TABLE IF NOT EXISTS app_permission (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    app_id UUID NOT NULL,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    group_name VARCHAR(500),
    description TEXT,
    include_permission_codes JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    CONSTRAINT fk_app_permission_app FOREIGN KEY (app_id) REFERENCES app(id)
);

CREATE TABLE IF NOT EXISTS app_role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    app_id UUID NOT NULL,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    CONSTRAINT fk_app_role_app FOREIGN KEY (app_id) REFERENCES app(id)
);

CREATE TABLE IF NOT EXISTS app_role_permission (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    CONSTRAINT fk_app_role_permission_role FOREIGN KEY (role_id) REFERENCES app_role(id),
    CONSTRAINT fk_app_role_permission_permission FOREIGN KEY (permission_id) REFERENCES app_permission(id),
    CONSTRAINT uq_app_role_permission_role_permission UNIQUE (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS app_access (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    app_id UUID NOT NULL,
    role_id UUID,
    scope TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    CONSTRAINT fk_app_access_account FOREIGN KEY (account_id) REFERENCES account(id),
    CONSTRAINT fk_app_access_app FOREIGN KEY (app_id) REFERENCES app(id),
    CONSTRAINT fk_app_access_role FOREIGN KEY (role_id) REFERENCES app_role(id),
    CONSTRAINT uq_app_access_account_app UNIQUE (account_id, app_id)
);

CREATE TABLE IF NOT EXISTS account_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    app_id UUID NOT NULL,
    expired_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_account_session_account FOREIGN KEY (account_id) REFERENCES account(id),
    CONSTRAINT fk_account_session_app FOREIGN KEY (app_id) REFERENCES app(id)
);
