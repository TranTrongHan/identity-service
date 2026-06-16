# Identity Service

A **Identity & Access Management (IAM)** microservice built with Java Spring Boot, implementing a flexible RBAC system extended with dynamic scope-based permission overrides.

---

## Key Features

- **Multi-tenant Identity** — one service manages auth for multiple applications (POS, CRM, HRM...), each with isolated signing keys, roles, and permissions
- **Extended RBAC with Scope Override** — per-account `+/-` scope allows adding or removing permissions without creating new roles
- **Parent-Child Permission Hierarchy** — assign a parent permission, automatically grant all children (transitive expansion)
- **Dynamic JWT Signing Keys** — per-app HMAC-SHA256 keys; token from App A cannot be used in App B
- **Revocable Refresh Tokens** — refresh token = server-side session UUID (not JWT), enabling force-logout and instant revocation
- **Force Logout** — admin can instantly invalidate all sessions + flag account for client-side detection
- **Brute Force Protection** — progressive lockout with configurable thresholds
- **Database Migrations** — versioned schema with Flyway

---

## Tech Stack

| Concern | Technology |
|---------|-----------|
| Framework | Spring Boot 3.5 (Java 21) |
| Security | Spring Security 6 + JJWT 0.12.6 (HMAC-SHA256) |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Migration | Flyway |
| Validation | Jakarta Bean Validation |
| Mapping | MapStruct 1.6 (compile-time) |
| API Docs | SpringDoc OpenAPI 2.8 (Swagger UI) |
| Build | Maven (multi-module) |

---

## Architecture

The project follows **Clean Architecture** with 4 Maven modules:

```
+-----------------------------------------------------------------+
|                           webapi                                 |
|  Controllers, Security Config, JWT Filter, Spring Boot entry    |
|  (Assembly module - builds executable JAR)                      |
+-----------------------------------------------------------------+
|         application            |        infrastructure           |
|  Services, DTOs, Use Cases     | JPA Entities, Repositories     |
|  Business logic orchestration  | MapStruct Mappers, Migrations  |
+-----------------------------------------------------------------+
|                           domain                                |
|  Pure POJOs, Enums, Exceptions, Repository Interfaces (Ports)   |
|  ZERO framework dependency                                      |
+-----------------------------------------------------------------+
```

**Dependency direction** (outer -> inner, never reverse):

```
webapi  ->  application  ->  domain
webapi  ->  infrastructure  ->  domain
```

---

## Project Structure

```
identity-service/
+-- pom.xml                          # Parent POM (version management)
|
+-- domain/                          # Core business layer
|   +-- entities/                    # Account, App, AppRole, AppAccess, etc.
|   +-- enums/                       # AuthFieldType, SettingCode
|   +-- exceptions/                  # Business exceptions
|   +-- repositories/                # Port interfaces
|
+-- application/                     # Use case layer
|   +-- services/                    # IdentityService, AccountService, etc.
|   +-- interfaces/                  # Service contracts
|   +-- dto/request/                 # LoginPasswordRequest, AccountFilterRequest, etc.
|   +-- dto/response/                # ApiResponse, PageResponse, TokenDataResponse, etc.
|   +-- helpers/                     # PasswordHelper, ScopeHelper, RandomHelper
|
+-- infrastructure/                  # Persistence & external adapters
|   +-- persistence/entities/        # JPA entities (@Entity, @Table)
|   +-- persistence/mappers/         # MapStruct mappers (Domain <-> JPA)
|   +-- persistence/jpa/             # Spring Data JPA repositories
|   +-- persistence/adapters/        # Port implementations
|   +-- persistence/converters/      # JPA AttributeConverters
|   +-- security/                    # TokenServiceImpl
|   +-- resources/db/migration/      # Flyway SQL migrations (V1-V4)
|
+-- webapi/                          # Presentation & assembly
|   +-- controller/common/           # IdentityController (public endpoints)
|   +-- controller/admin/            # AdminAccountController (admin endpoints)
|   +-- security/                    # JwtAuthenticationFilter
|   +-- config/                      # SecurityConfig
|   +-- exception/                   # GlobalExceptionHandler
|
+-- docs/                            # API implementation checklist
```

---

## Database Schema

Tables (Flyway V1-V4):

```
account                    account_auth              account_session
+------------------+       +------------------+      +------------------+
| id (UUID, PK)    |--1:N--| id (UUID, PK)    |     | id (UUID, PK)    |
| name             |       | account_id (FK)  |     | account_id (FK)  |
| avatar_url       |       | field_type (INT) |     | app_id (FK)      |
| secret_key       |       | field_value      |     | expired_at       |
| password         |       | created_at       |     | created_at       |
| wrong_login_count|       +------------------+      +------------------+
| access_denied_until|
| created_at       |      account_logout
| deleted_at       |      +------------------+
+------------------+      | id (UUID, PK)    |
                          | account_id (FK)  |
                          | created_at       |
app                       +------------------+
+------------------+
| id (UUID, PK)    |      app_access
| code (UNIQUE)    |      +------------------+
| name             |      | id (UUID, PK)    |
| description      |      | account_id (FK)  |
| signing_key(128) |      | app_id (FK)      |
| token_lifetime   |      | role_id (FK)     |
| session_lifetime |      | scope (TEXT)     |
| created_at       |      | created_at       |
| deleted_at       |      | deleted_at       |
+------------------+      +------------------+

app_role                   app_permission             app_role_permission
+------------------+       +------------------+       +------------------+
| id (UUID, PK)    |       | id (UUID, PK)    |       | id (UUID, PK)    |
| app_id (FK)      |       | app_id (FK)      |       | role_id (FK)     |
| code             |       | code             |       | permission_id(FK)|
| name             |       | name             |       +------------------+
| created_at       |       | group_name       |
| deleted_at       |       | description      |       setting
+------------------+       | include_codes(J) |       +------------------+
                           | created_at       |       | code (PK)        |
                           | deleted_at       |       | value            |
                           +------------------+       | description      |
                                                      +------------------+
```

---

## API Endpoints (Implemented)

### Public (no auth required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/Identity/Login/Password` | Login with username + password |
| POST | `/Identity/RefreshToken` | Refresh access token using session UUID |

### Authenticated (Bearer token required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/Identity/ForceLogout` | Check if current account is force-logged-out (one-shot) |

### Admin (requires `admin` scope)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/Admin/Account` | Create new account |
| GET | `/Admin/Account` | List accounts (paginated, filterable) |
| GET | `/Admin/Account/{id}` | Get account detail with relations |
| PUT | `/Admin/Account/{id}/Password` | Set/reset password |
| DELETE | `/Admin/Account/{id}` | Soft-delete account |
| PUT | `/Admin/Account/{id}/ForceLogout` | Force logout account |
| GET | `/Admin/Account/{id}/ForceLogout` | Check force-logout status |

See [docs/API_IMPLEMENTATION_CHECKLIST.md](docs/API_IMPLEMENTATION_CHECKLIST.md) for full planned API list.

---

## Core Algorithm: Scope Resolution

The scope mechanism extends standard RBAC to support per-account permission overrides:

```
Input:  AppAccess { role: "manager", scope: "+export.data -order.delete" }
        Role "manager" permissions: [order.create, order.view, order.delete, order.report]
        Permission "order.create" includes: [order.view]

Algorithm:
  1. Get role permissions -> {order.create, order.view, order.delete, order.report}
  2. Parse scope string -> positive: {export.data}, negative: {order.delete}
  3. Merge: role_perms + positive = {order.create, order.view, order.delete, order.report, export.data}
  4. Expand parent->children (transitive): order.create -> adds order.view (already present)
  5. Apply negatives: remove order.delete

Output: "order.create order.view order.report export.data"
```

---

## How to Run

### Prerequisites
- Java 21+
- PostgreSQL
- Maven 3.9+

### Quick Start

```bash
# 1. Start PostgreSQL (Docker)
docker run -d --name pg-identity \
  -e POSTGRES_DB=identity \
  -e POSTGRES_PASSWORD=admin \
  -p 5432:5432 postgres:16

# 2. Build all modules (required for multi-module)
mvn clean install -DskipTests

# 3. Run database migrations
mvn flyway:migrate -pl :infrastructure -Dflyway.password=admin

# 4. Start the application
mvn spring-boot:run -pl :webapi
```

### Access
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html

### Build Commands

```bash
mvn clean install -DskipTests         # Build + install all modules (required before run)
mvn spring-boot:run -pl :webapi       # Start application
mvn flyway:migrate -pl :infrastructure -Dflyway.password=xxx  # Apply migrations
mvn dependency:tree -pl webapi        # Check dependency tree
```

**Important:** Always use `mvn clean install` (not just `compile`) before running. Multi-module projects need JARs installed to local repo for cross-module resolution.

---

## Design Decisions

| Decision | Rationale |
|----------|-----------|
| **Scope override instead of more roles** | Avoids role explosion in systems with 100+ permissions |
| **Dynamic per-app signing key** | Security isolation between tenants. Key rotation per-app. |
| **Refresh token = session UUID (not JWT)** | Server-side revocation. Force-logout deletes session instantly. |
| **Password hash with per-account secret key** | Extra entropy beyond BCrypt salt. Secret key stored alongside account. |
| **Strict Clean Architecture (domain = pure POJO)** | Domain logic testable without Spring. Infrastructure swappable. |
| **MapStruct over ModelMapper** | Compile-time generation = zero reflection, type-safe, debuggable |
| **Flyway over Hibernate ddl-auto** | Production-safe. Versioned, reviewable migrations. |
| **Soft-delete pattern** | Set `deleted_at` timestamp instead of hard delete. Recoverable. |

---

## Project Goals

This project aims to implement a full-featured IAM service. Progress is tracked in [docs/API_IMPLEMENTATION_CHECKLIST.md](docs/API_IMPLEMENTATION_CHECKLIST.md), organized by priority:

- **Level 1 (Core):** Account CRUD, password management, force-logout
- **Level 2 (RBAC Admin):** App, AppRole, AppPermission, AppAccess management
- **Level 3 (Advanced):** OAuth login, reset password, site management
