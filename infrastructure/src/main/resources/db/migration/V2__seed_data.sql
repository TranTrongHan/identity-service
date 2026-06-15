-- V2__seed_data.sql
-- Purpose: Seed essential data for system bootstrap
-- Reference: source mẫu DataSeeds (Account, App, AppAccess, AccountAuth, Setting)

-- ============================================================
-- 1. IDENTITY App
-- Đây là app quản lý auth, mọi admin endpoint yêu cầu issuer = "IDENTITY"
-- SigningKey: 128 chars (HMAC-SHA256 requires minimum 256 bits = 32 bytes)
-- TokenLifetime: 10 phút (ngắn hơn để secure)
-- SessionLifetime: 240 phút (4 tiếng)
-- ============================================================
INSERT INTO app (id, code, name, description, signing_key, token_lifetime_minutes, session_lifetime_minutes, created_at)
VALUES (
    '4e4215f0-f18f-4358-ada4-f330116e192b',
    'IDENTITY',
    'Identity Service',
    'Service xác thực và phân quyền cho toàn hệ thống',
    'SHOCF14EDHFHEOJCNL7EUC19AWSGIY98FJJDFXAIQIOJ8EDRKVEG4BSPYER3P82RCZ1PP0R56P12VN3DFHX76HBU6N4TO5N98ABDLLQSSWFTEZBQNMDE0QLUWOY5VXH4',
    10,
    240,
    NOW()
);

-- ============================================================
-- 2. Root Account
-- Username: root / Password: root
-- SecretKey dùng làm pepper khi hash password (BCrypt)
-- Password hash = BCrypt("root" + "khWMO6wl7cv8wln")
-- Tạo bằng: PasswordHelper.hashPassword("root", "khWMO6wl7cv8wln")
-- ============================================================
INSERT INTO account (id, name, secret_key, password, wrong_login_count, created_at)
VALUES (
    '653dc4d4-ca05-45ac-83cd-e98fa91b890f',
    'Root Administrator',
    'khWMO6wl7cv8wln',
    '$2a$10$rOzCq7YRiPHFAhMA6YLBGuWCNXP3KXSLNEd9PX3Cxo1bMYAsC5LWi',
    0,
    NOW()
);

-- ============================================================
-- 3. Root AccountAuth (login credential: username = "root")
-- FieldType 1 = USERNAME (enum AuthFieldType.USERNAME)
-- ============================================================
INSERT INTO account_auth (id, account_id, field_type, field_value, created_at)
VALUES (
    '4e2213f6-db9d-405f-8716-5fc73ae703a3',
    '653dc4d4-ca05-45ac-83cd-e98fa91b890f',
    1,
    'root',
    NOW()
);

-- ============================================================
-- 4. Root AppAccess (admin access to IDENTITY app)
-- Scope "admin" = super permission, bypass tất cả permission checks
-- ============================================================
INSERT INTO app_access (id, account_id, app_id, scope, created_at)
VALUES (
    'b298c37e-c7ee-48aa-91ff-79256471e4ac',
    '653dc4d4-ca05-45ac-83cd-e98fa91b890f',
    '4e4215f0-f18f-4358-ada4-f330116e192b',
    'admin',
    NOW()
);

