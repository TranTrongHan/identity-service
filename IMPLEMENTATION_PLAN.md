# Implementation Plan: Identity & Authorization Service (Java Spring Boot)

> Hệ thống phân quyền tinh giản từ EggstechIdentity.API (.NET), rebuild bằng Java Spring Boot.
> Mục tiêu: đủ sâu để gây ấn tượng fresher interview, không thừa feature gây loãng.

---

## Tech Stack

| Layer | Công nghệ |
|-------|-----------|
| Framework | Spring Boot 3.x (Java 17+) |
| Security | Spring Security 6 + JJWT |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Migration | Flyway |
| Validation | Jakarta Bean Validation |
| Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven hoặc Gradle |
| Container | Docker + docker-compose |
| Test | JUnit 5 + Mockito |
| Cache | Spring Cache (Caffeine - in-memory) |

---

## Database Schema

### ERD tổng quan

```
┌──────────────┐       ┌──────────────────┐       ┌───────────────┐
│   account    │──1:N──│   account_auth   │       │    setting    │
│              │       │ (login methods)   │       │  (key-value)  │
│ - id (UUID)  │       │ - field_type      │       └───────────────┘
│ - name       │       │ - field_value     │
│ - secret_key │       └──────────────────┘
│ - password   │
│ - wrong_count│       ┌──────────────────┐
│ - denied_until       │   app_access     │──N:1──┐
│ - created_at │──1:N──│                  │       │
│ - deleted_at │       │ - account_id     │       │
└──────────────┘       │ - app_id         │       │
                       │ - role_id (nullable)      │
                       │ - scope           │       │
                       └──────────────────┘       │
                                                   │
┌──────────────┐       ┌──────────────────┐       │
│   app_role   │──1:N──│app_role_permission│       │
│              │       │ (junction table) │       │
│ - id         │       │ - role_id        │       │
│ - code       │       │ - permission_id  │       │
│ - name       │       └────────┬─────────┘       │
│ - app_id     │                │                  │
└──────┬───────┘                │                  │
       │                        │                  │
       │               ┌────────┴─────────┐       │
       │               │  app_permission   │       │
       │               │                   │       │
       │               │ - id              │       │
       │               │ - code            │       │
       │               │ - name            │       │
       │               │ - group_name      │       │
       │               │ - include_codes   │       │
       │               │ - app_id          │       │
       │               └───────────────────┘       │
       │                                           │
       └───────────────────────────────────────────┘
                                                   │
                              ┌─────────────────┐  │
                              │       app       │──┘
                              │                 │
                              │ - id            │
                              │ - code (unique) │
                              │ - name          │
                              │ - signing_key   │
                              │ - token_lifetime│
                              │ - session_lifetime
                              └─────────────────┘

┌──────────────────┐
│ account_session  │
│                  │
│ - id (= refresh token)
│ - account_id    │
│ - app_id        │
│ - expired_at    │
│ - created_at    │
└──────────────────┘
```

### Tables chi tiết

#### `app`
```sql
CREATE TABLE app (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    signing_key VARCHAR(128) NOT NULL,          -- HMAC-SHA256 key
    token_lifetime_minutes INT NOT NULL DEFAULT 15,
    session_lifetime_minutes INT NOT NULL DEFAULT 1440,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);
```

#### `account`
```sql
CREATE TABLE account (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    avatar_url TEXT,
    secret_key VARCHAR(100) NOT NULL,           -- salt cho password hash
    password VARCHAR(100),                       -- BCrypt hash
    wrong_login_count INT NOT NULL DEFAULT 0,
    access_denied_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);
```

#### `account_auth`
```sql
CREATE TABLE account_auth (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL REFERENCES account(id),
    field_type INT NOT NULL,                    -- 1=USERNAME, 2=EMAIL, 3=PHONE
    field_value VARCHAR(200) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(field_type, field_value)
);
```

#### `app_permission`
```sql
CREATE TABLE app_permission (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    app_id UUID NOT NULL REFERENCES app(id),
    code VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    group_name VARCHAR(500),                    -- "Đơn hàng>>Quản lý" (nested group)
    description TEXT,
    include_permission_codes JSONB,             -- ["child.code1", "child.code2"]
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);
```

#### `app_role`
```sql
CREATE TABLE app_role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    app_id UUID NOT NULL REFERENCES app(id),
    code VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);
```

#### `app_role_permission`
```sql
CREATE TABLE app_role_permission (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id UUID NOT NULL REFERENCES app_role(id),
    permission_id UUID NOT NULL REFERENCES app_permission(id),
    UNIQUE(role_id, permission_id)
);
```

#### `app_access`
```sql
CREATE TABLE app_access (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL REFERENCES account(id),
    app_id UUID NOT NULL REFERENCES app(id),
    role_id UUID REFERENCES app_role(id),
    scope TEXT NOT NULL DEFAULT '',             -- "+extra.perm -removed.perm"
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    UNIQUE(account_id, app_id)
);
```

#### `account_session`
```sql
CREATE TABLE account_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),  -- ID này = refresh token
    account_id UUID NOT NULL REFERENCES account(id),
    app_id UUID NOT NULL REFERENCES app(id),
    expired_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## Project Structure

```
identity-service/
├── docker-compose.yml
├── Dockerfile
├── README.md
├── pom.xml (hoặc build.gradle)
│
├── src/
│   ├── main/
│   │   ├── java/com/eggstech/identity/
│   │   │   ├── IdentityApplication.java
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtConfig.java
│   │   │   │   ├── CacheConfig.java
│   │   │   │   └── OpenApiConfig.java
│   │   │   │
│   │   │   ├── entity/
│   │   │   │   ├── BaseEntity.java             # id, createdAt, deletedAt
│   │   │   │   ├── Account.java
│   │   │   │   ├── AccountAuth.java
│   │   │   │   ├── App.java
│   │   │   │   ├── AppRole.java
│   │   │   │   ├── AppPermission.java
│   │   │   │   ├── AppRolePermission.java
│   │   │   │   ├── AppAccess.java
│   │   │   │   └── AccountSession.java
│   │   │   │
│   │   │   ├── enums/
│   │   │   │   └── AuthFieldType.java          # USERNAME(1), EMAIL(2), PHONE(3)
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── AccountRepository.java
│   │   │   │   ├── AccountAuthRepository.java
│   │   │   │   ├── AppRepository.java
│   │   │   │   ├── AppRoleRepository.java
│   │   │   │   ├── AppPermissionRepository.java
│   │   │   │   ├── AppAccessRepository.java
│   │   │   │   └── AccountSessionRepository.java
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java            # login, refresh
│   │   │   │   ├── TokenService.java           # JWT generate/validate
│   │   │   │   ├── ScopeResolver.java          # ★ Core scope logic
│   │   │   │   ├── AccountService.java         # CRUD + unlock + force logout
│   │   │   │   ├── AppService.java
│   │   │   │   ├── AppRoleService.java
│   │   │   │   └── AppPermissionService.java
│   │   │   │
│   │   │   ├── security/
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── DynamicSigningKeyResolver.java
│   │   │   │   ├── SecurityContext.java        # Current user holder
│   │   │   │   └── RequireScope.java           # Custom annotation
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java         # Login, Refresh, ForceLogout check
│   │   │   │   ├── ProfileController.java      # GET /me
│   │   │   │   └── admin/
│   │   │   │       ├── AccountAdminController.java
│   │   │   │       ├── AppAdminController.java
│   │   │   │       ├── RoleAdminController.java
│   │   │   │       └── PermissionAdminController.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── RefreshTokenRequest.java
│   │   │   │   │   ├── CreateAccountRequest.java
│   │   │   │   │   ├── CreateAppRequest.java
│   │   │   │   │   ├── CreateRoleRequest.java
│   │   │   │   │   ├── AssignPermissionsRequest.java
│   │   │   │   │   ├── CreatePermissionRequest.java
│   │   │   │   │   └── GrantAccessRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── ApiResponse.java        # Generic {success, data, error}
│   │   │   │       ├── TokenResponse.java      # {accessToken, refreshToken, scope, role}
│   │   │   │       ├── ProfileResponse.java
│   │   │   │       ├── PageResponse.java       # Pagination wrapper
│   │   │   │       └── PermissionTreeResponse.java  # Grouped permission tree
│   │   │   │
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       ├── AuthenticationException.java
│   │   │       ├── AccessDeniedException.java
│   │   │       ├── ResourceNotFoundException.java
│   │   │       └── BruteForceException.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── db/migration/
│   │           ├── V1__init_schema.sql
│   │           ├── V2__seed_data.sql
│   │           └── V3__indexes.sql
│   │
│   └── test/java/com/eggstech/identity/
│       ├── service/
│       │   ├── ScopeResolverTest.java          # ★ Unit test quan trọng nhất
│       │   ├── TokenServiceTest.java
│       │   └── AuthServiceTest.java
│       └── controller/
│           └── AuthControllerIntegrationTest.java
```

---

## Implementation Phases

---

### Phase 1: Project Setup & Foundation

#### 1.1 Spring Boot project initialization
- [ ] Init Spring Boot 3.x project (Spring Web, Spring Data JPA, Spring Security, Validation)
- [ ] Setup `pom.xml` / `build.gradle` dependencies:
  - `spring-boot-starter-web`
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-security`
  - `spring-boot-starter-validation`
  - `spring-boot-starter-cache`
  - `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (io.jsonwebtoken 0.12.x)
  - `postgresql` driver
  - `flyway-core`
  - `springdoc-openapi-starter-webmvc-ui`
  - `caffeine` (cache)
  - `lombok` (optional, giảm boilerplate)

#### 1.2 Docker setup
- [ ] `docker-compose.yml`: PostgreSQL 16 + pgAdmin (optional)
- [ ] `Dockerfile`: multi-stage build (Maven build → JRE runtime)
- [ ] `.env` file cho DB credentials

#### 1.3 Configuration
- [ ] `application.yml`: datasource, JPA, flyway, JWT properties
- [ ] `application-dev.yml`: dev-specific config (show SQL, debug logging)

#### 1.4 Base classes
- [ ] `BaseEntity.java`: id (UUID), createdAt, deletedAt
- [ ] `ApiResponse<T>`: generic response wrapper
- [ ] `GlobalExceptionHandler.java`: `@RestControllerAdvice`

---

### Phase 2: Entity & Repository Layer

#### 2.1 Entities
- [ ] `Account.java` — fields: name, secretKey, password, wrongLoginCount, accessDeniedUntil
- [ ] `AccountAuth.java` — fields: accountId, fieldType (enum), fieldValue
- [ ] `App.java` — fields: code, name, signingKey, tokenLifetimeMinutes, sessionLifetimeMinutes
- [ ] `AppRole.java` — fields: code, name, appId
- [ ] `AppPermission.java` — fields: code, name, groupName, description, includePermissionCodes (JSON), appId
- [ ] `AppRolePermission.java` — junction: roleId, permissionId
- [ ] `AppAccess.java` — fields: accountId, appId, roleId (nullable), scope
- [ ] `AccountSession.java` — fields: accountId, appId, expiredAt

#### 2.2 Repositories (Spring Data JPA)
- [ ] `AccountRepository` — findById, existsByIdAndDeletedAtIsNull
- [ ] `AccountAuthRepository` — findByFieldTypeAndFieldValue
- [ ] `AppRepository` — findByCode, findByCodeAndDeletedAtIsNull
- [ ] `AppRoleRepository` — findByAppIdAndDeletedAtIsNull, findByCodeAndAppId
- [ ] `AppPermissionRepository` — findAllByAppIdAndDeletedAtIsNull
- [ ] `AppRolePermissionRepository` — findAllByRoleId
- [ ] `AppAccessRepository` — findByAccountIdAndAppId
- [ ] `AccountSessionRepository` — findByIdAndExpiredAtAfter, deleteAllByExpiredAtBefore

#### 2.3 Database migrations (Flyway)
- [ ] `V1__init_schema.sql` — tất cả tables ở trên
- [ ] `V2__seed_data.sql` — root account + IDENTITY app + admin access
- [ ] `V3__indexes.sql` — indexes cho performance

---

### Phase 3: Authentication (JWT + Refresh Token)

#### 3.1 TokenService
- [ ] `generateAccessToken(Account, App, String effectiveScope, AppRole role)`:
  - Claims: id, name, avatarUrl, roleCode, roleName, scope
  - Issuer = app.code
  - Expiry = now + app.tokenLifetimeMinutes
  - Sign với app.signingKey (HMAC-SHA256)
- [ ] `parseToken(String token)` — parse without validation (to read issuer)
- [ ] `validateToken(String token, String signingKey)` — full validate

#### 3.2 DynamicSigningKeyResolver
- [ ] Resolve signing key từ DB by app code (from JWT issuer)
- [ ] Cache with Caffeine (TTL 30 phút)
- [ ] `@Cacheable("signingKeys")` + `@CacheEvict` khi update app

#### 3.3 JwtAuthenticationFilter (OncePerRequestFilter)
- [ ] Extract token from `Authorization: Bearer xxx`
- [ ] Parse token → get issuer (app code)
- [ ] Resolve signing key by app code
- [ ] Validate token signature + expiry
- [ ] Set `SecurityContextHolder` với authentication object
- [ ] Skip filter cho public endpoints (login, refresh, swagger)

#### 3.4 AuthService — Login
- [ ] `login(String fieldValue, String password, String appCode)`:
  1. Tìm `AccountAuth` by field value
  2. Load `Account`, check deletedAt (inactive)
  3. Check brute force: nếu `accessDeniedUntil > now` → throw
  4. Verify password: `BCrypt.matches(password, account.password)`
  5. Nếu sai → tăng wrongLoginCount, tính lockout time, save
  6. Nếu đúng → reset wrongLoginCount
  7. Load `AppAccess` (account + app) → check tồn tại
  8. Resolve effective scope (xem Phase 4)
  9. Create/extend `AccountSession`
  10. Generate access token
  11. Return `TokenResponse{accessToken, refreshToken, scope, roleCode}`

#### 3.5 AuthService — Refresh Token
- [ ] `refresh(UUID refreshToken, String appCode)`:
  1. Tìm `AccountSession` by id = refreshToken
  2. Check expiredAt > now (chưa hết hạn)
  3. Extend expiredAt = now + app.sessionLifetimeMinutes
  4. Re-generate access token (claims mới nhất từ DB)
  5. Return new `TokenResponse`

#### 3.6 Brute Force Protection
- [ ] Config: `max-wrong-login: 3`, `lockout-minutes-per-wrong: 5`
- [ ] Logic: lần thứ N sai (N > max) → lock `(N - max + 1) * lockoutMinutes` phút
- [ ] Admin unlock endpoint: `PUT /admin/accounts/{id}/unlock`

#### 3.7 Session Cleanup
- [ ] `@Scheduled(cron = "0 0 2 * * *")` — xóa expired sessions hàng ngày 2:00 AM

---

### Phase 4: Authorization (Scope Resolution) — ★ PHẦN QUAN TRỌNG NHẤT

#### 4.1 ScopeResolver — Core logic
- [ ] `resolve(AppAccess access, List<AppPermission> allPermissions)` → `Set<String>`:

```
Algorithm:
  1. Parse scope string → tách thành positiveSet và negativeSet
     - "admin +order.create -order.delete payment.view"
     - positive: {admin, order.create, payment.view}
     - negative: {order.delete}

  2. Get role permissions (nếu có role)
     - Query AppRolePermission → lấy tất cả permission codes của role
     - VD: role "manager" → {order.create, order.view, order.delete, order.report}

  3. Merge:
     - Start với role permissions
     - Thêm tất cả positive scope items (dù role không có)
     - Loại bỏ tất cả negative scope items

  4. Expand parent-child:
     - Với mỗi permission trong effective set:
       - Nếu có includePermissionCodes → thêm tất cả children
     - Lặp cho đến khi không còn permission mới được thêm (transitive)

  5. Apply negative lần cuối:
     - Loại bỏ tất cả negative codes (và children của chúng nếu negative là parent)

  6. Return: final effective permission set
```

#### 4.2 @RequireScope — Custom authorization annotation
- [ ] Annotation definition:
  ```java
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface RequireScope {
      String[] value();  // required permission codes (OR logic)
  }
  ```
- [ ] AOP Aspect hoặc `HandlerInterceptor`:
  - Read annotation value
  - Get current user's scope from SecurityContext
  - Check scope contains at least one required permission
  - Nếu không → throw AccessDeniedException

#### 4.3 SecurityContext (current user info)
- [ ] `SecurityContext.java` — thread-local holder:
  ```java
  // Accessible anywhere via:
  SecurityContext.getCurrentUser() → {id, name, scope, roleCode, appCode}
  ```

---

### Phase 5: Admin API Endpoints

#### 5.1 App Management
- [ ] `POST /admin/apps` — tạo app mới (auto-generate 128-char signing key)
- [ ] `GET /admin/apps` — list apps (paginated)
- [ ] `GET /admin/apps/{id}` — chi tiết app
- [ ] `PUT /admin/apps/{id}` — update app info
- [ ] `DELETE /admin/apps/{id}` — soft delete

#### 5.2 Account Management
- [ ] `POST /admin/accounts` — tạo account (với auth methods)
- [ ] `GET /admin/accounts` — list (paginated, filter by name/status)
- [ ] `GET /admin/accounts/{id}` — chi tiết
- [ ] `PUT /admin/accounts/{id}` — update
- [ ] `PUT /admin/accounts/{id}/set-password` — set/reset password
- [ ] `PUT /admin/accounts/{id}/unlock` — unlock brute force
- [ ] `POST /admin/accounts/{id}/force-logout` — buộc logout
- [ ] `DELETE /admin/accounts/{id}` — soft delete

#### 5.3 Permission Management
- [ ] `POST /admin/apps/{appId}/permissions` — tạo permission
- [ ] `GET /admin/apps/{appId}/permissions` — list (flat)
- [ ] `GET /admin/apps/{appId}/permissions/tree` — ★ grouped tree (by group_name `>>`)
- [ ] `PUT /admin/permissions/{id}` — update (code, name, group, includePermissionCodes)
- [ ] `DELETE /admin/permissions/{id}` — soft delete

#### 5.4 Role Management
- [ ] `POST /admin/apps/{appId}/roles` — tạo role
- [ ] `GET /admin/apps/{appId}/roles` — list roles
- [ ] `GET /admin/roles/{id}` — chi tiết (kèm permission list)
- [ ] `PUT /admin/roles/{id}` — update
- [ ] `PUT /admin/roles/{id}/permissions` — ★ gán/thay đổi permissions cho role
- [ ] `DELETE /admin/roles/{id}` — soft delete

#### 5.5 Access Management (Account ↔ App)
- [ ] `POST /admin/access` — grant access: gắn account vào app + role + scope
- [ ] `GET /admin/apps/{appId}/access` — list accounts có quyền truy cập app
- [ ] `PUT /admin/access/{id}` — update role/scope
- [ ] `DELETE /admin/access/{id}` — revoke access (soft delete)

#### 5.6 Admin endpoints security
- [ ] Tất cả `/admin/**` require scope "admin" + issuer "IDENTITY"
- [ ] Implement bằng `@RequireScope("admin")` + check issuer trong filter

---

### Phase 6: Public API Endpoints

#### 6.1 Auth endpoints (no auth required)
- [ ] `POST /auth/login` — login bằng password
- [ ] `POST /auth/refresh` — refresh token
- [ ] `GET /auth/force-logout` — check có bị force logout không (cần auth)

#### 6.2 Profile endpoint (auth required, any app)
- [ ] `GET /me` — thông tin user hiện tại (từ JWT claims)
- [ ] `GET /me/permissions` — full permission list (resolved)

---

### Phase 7: Testing

#### 7.1 Unit Tests (★ quan trọng nhất)
- [ ] `ScopeResolverTest.java`:
  - Test case: scope chỉ có positive → effective = positives
  - Test case: role + no scope override → effective = role permissions
  - Test case: role + positive override → thêm permissions ngoài role
  - Test case: role + negative override → loại bỏ permissions khỏi role
  - Test case: parent-child expansion → gán parent tự động có children
  - Test case: negative parent → loại bỏ cả subtree
  - Test case: empty scope + no role → empty permissions
  - Test case: scope "admin" → include "admin" in effective set

- [ ] `TokenServiceTest.java`:
  - Test generate token → contains correct claims
  - Test validate token → success with correct key
  - Test validate token → fail with wrong key (different app)
  - Test expired token → rejected

- [ ] `AuthServiceTest.java`:
  - Test login success → returns tokens
  - Test login wrong password → increment counter
  - Test login brute force locked → throw exception
  - Test login account inactive → throw exception
  - Test login no app access → throw exception
  - Test refresh token success → new access token
  - Test refresh token expired → throw exception

#### 7.2 Integration Tests
- [ ] `AuthControllerIntegrationTest.java`:
  - Full login flow with real DB (Testcontainers or H2)
  - Refresh flow
  - Admin endpoint requires admin scope
  - Non-admin rejected from admin endpoints

---

### Phase 8: Polish & Documentation

#### 8.1 API Documentation
- [ ] Swagger annotations trên controllers
- [ ] Group endpoints by tag (Auth, Admin/Account, Admin/App, Admin/Role, Admin/Permission)
- [ ] Example request/response bodies

#### 8.2 Error Handling
- [ ] `GlobalExceptionHandler`:
  - `AuthenticationException` → 401
  - `AccessDeniedException` → 403
  - `ResourceNotFoundException` → 404
  - `BruteForceException` → 429 (Too Many Requests)
  - `MethodArgumentNotValidException` → 400 (validation errors)
  - Fallback `Exception` → 500
- [ ] Consistent error response format:
  ```json
  {
    "success": false,
    "error": {
      "code": "BRUTE_FORCE_LOCKED",
      "message": "Account locked. Try again after 5 minutes.",
      "details": {}
    }
  }
  ```

#### 8.3 Request Validation
- [ ] `@NotBlank`, `@Size`, `@Email` trên request DTOs
- [ ] Custom validator cho scope format (nếu cần)

#### 8.4 Docker & deployment
- [ ] Multi-stage Dockerfile
- [ ] docker-compose: app + PostgreSQL
- [ ] Health check endpoint: `GET /actuator/health`

#### 8.5 README.md
- [ ] Project overview + architecture diagram
- [ ] How to run (docker-compose up)
- [ ] API examples (curl commands)
- [ ] Design decisions explained (tại sao dynamic key, tại sao scope mechanism)
- [ ] References (link tới hệ thống .NET gốc mà bạn learned from)

---

## Những thứ KHÔNG làm (đã tinh giản)

| Feature bỏ | Lý do |
|-------------|-------|
| Site-level permission (AccountSite) | Quá niche, tăng complexity mà recruiter không evaluate được |
| OAuth providers (Google/Facebook/Zalo) | Chỉ là call API bên thứ 3, không thể hiện kỹ năng |
| Email service / Reset password | Nice-to-have nhưng out of scope cho auth core |
| Generic CRUD endpoint generation | Pattern riêng của .NET framework, Java dùng cách khác |
| Permission Type (Global/Site/Both) | Chỉ cần khi có site-level, đã bỏ |
| AccountField (custom fields) | Phụ trợ, không liên quan auth |
| Multiple OAuth state tables | Không cần khi bỏ OAuth |
| Hangfire (complex job scheduling) | `@Scheduled` đủ cho session cleanup |

---

## Điểm nhấn khi Interview

Khi trình bày project này, focus vào 5 điểm:

1. **"Tại sao scope mechanism thay vì RBAC đơn giản?"**
   → Real-world problem: role "manager" có 50 quyền, nhưng muốn 1 account cụ thể KHÔNG có quyền xóa data dù là manager. Scope override giải quyết mà không cần tạo role mới.

2. **"Tại sao dynamic signing key?"**
   → Security isolation: token app A không dùng được cho app B. Có thể rotate key 1 app mà không ảnh hưởng app khác.

3. **"Tại sao refresh token = session ID chứ không phải JWT?"**
   → Revocable: khi cần force logout, chỉ cần delete session record. Nếu refresh token là JWT stateless thì không revoke được.

4. **"Parent-child permission giải quyết gì?"**
   → DRY: thay vì gán 10 quyền con cho role, chỉ cần gán 1 quyền cha. Khi thêm quyền con mới, tất cả roles có quyền cha tự động có quyền mới.

5. **"Multi-tenant identity service benefit gì?"**
   → Microservices: 1 service quản lý auth cho toàn bộ hệ thống. Mỗi microservice (POS, CRM, HRM) không cần tự implement auth logic.

---

## Quick Reference: Key Algorithms

### Password Verification
```
Input: rawPassword, account.password (BCrypt hash)
Verify: BCryptPasswordEncoder.matches(rawPassword, storedHash)
```

### Brute Force Lockout
```
if wrongLoginCount >= maxAllowed:
    lockMinutes = (wrongLoginCount - maxAllowed + 1) * lockoutMinutesPerWrong
    accessDeniedUntil = now + lockMinutes
```

### Scope Resolution
```
1. Parse scope → {positive[], negative[]}
2. rolePerms = role ? role.permissions : {}
3. effective = rolePerms ∪ positive
4. effective = expandChildren(effective)   // parent → auto-include children
5. effective = effective - negative         // remove overrides
6. effective = effective - childrenOf(negative)  // remove subtree of negated parents
```

### Token Generation
```
claims = {id, name, roleCode, roleName, scope: effective.join(" ")}
key = hmacShaKey(app.signingKey)
token = JWT.sign(claims, key, HS256, expiry: now + app.tokenLifetimeMinutes)
```
