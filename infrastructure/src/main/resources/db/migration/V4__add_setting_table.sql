-- V4__add_setting_table.sql
-- Purpose: Create setting table for system configuration (brute force, etc.)

CREATE TABLE IF NOT EXISTS setting (
    code VARCHAR(100) PRIMARY KEY,
    value VARCHAR(500),
    description VARCHAR(500)
);

-- Seed login-related settings
INSERT INTO setting (code, value, description) VALUES
    ('MAX_WRONG_LOGIN_ALLOWED', '3', 'Số lần login sai cho phép trước khi bị khóa tạm'),
    ('WAIT_MINUTE_PER_WRONG_LOGIN', '5', 'Số phút phải chờ mỗi lần login sai (progressive lockout)');
