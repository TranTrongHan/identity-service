# Identity Service

A production-grade **Identity & Access Management (IAM)** microservice built with Java Spring Boot, implementing a flexible RBAC system extended with dynamic scope-based permission overrides.

Rebuilt from a production .NET system into Java to demonstrate deep understanding of authentication/authorization architecture, clean code organization, and enterprise design patterns.

---

## Key Features

- **Multi-tenant Identity** — one service manages auth for multiple applications (POS, CRM, HRM...), each with isolated signing keys, roles, and permissions
- **Extended RBAC with Scope Override** — beyond traditional RBAC: per-account `+/-` scope allows adding or removing permissions without creating new roles
- **Parent-Child Permission Hierarchy** — assign a parent permission, automatically grant all children (transitive expansion)
- **Dynamic JWT Signing Keys** — per-app HMAC-SHA256 keys with cache; token from App A cannot be used in App B
- **Revocable Refresh Tokens** — refresh token = server-side session ID (not JWT), enabling force-logout and instant revocation
- **Brute Force Protection** — progressive lockout with configurable thresholds
- **Database Migrations** — versioned schema with Flyway

---

## Tech Stack

| Concern | Technology |
|---------|-----------|
| Framework | Spring Boot 3.5 (Java 21) |
| Security | Spring Security 6 + JJWT (HMAC-SHA256) |
| Database | PostgreSQL 18 |
| ORM | Spring Data JPA / Hibernate |
| Migration | Flyway |
| Validation | Jakarta Bean Validation |
| Mapping | MapStruct 1.6 (compile-time) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven (multi-module) |
| Cache | Spring Cache + Caffeine |
| Test | JUnit 5 + Mockito + Testcontainers |

---

## Architecture

The project follows **strict Clean Architecture** (Hexagonal / Ports & Adapters) with 4 Maven modules:

```
┌───────────────────────────────────────────────────────────────┐
│                         webapi                                  │
│  Controllers, Security Config, Spring Boot entry point         │
│  (Assembly module — builds executable JAR)                     │
├───────────────────────────────────────────────────────────────┤
│           application                 infrastructure            │
│  Services, DTOs, Use Cases    │  JPA Entities, Repositories   │
│  Business logic orchestration │  MapStruct Mappers, Migrations │
├───────────────────────────────────────────────────────────────┤
│                          domain                                │
│  Pure POJOs, Enums, Exceptions, Repository Interfaces (Ports) │
│  *** ZERO framework dependency ***                            │
└───────────────────────────────────────────────────────────────┘
```

**Dependency direction** (outer → inner, never reverse):

```
webapi  →  application  →  domain
webapi  →  infrastructure  →  domain
```

### Why strict Clean Architecture?

- **Domain** is a plain Java module — no Spring, no JPA annotations, no framework lock-in
- **Infrastructure** has its own JPA entities + MapStruct mappers to convert to/from domain objects
- Swap PostgreSQL for MongoDB? Only `infrastructure` changes
- Swap Spring for Quarkus? Only `webapi` changes
- Business logic is testable with zero framework overhead

---

## Project Structure

```
identity-service/
├── pom.xml                          # Parent POM (version management, shared plugins)
│
├── domain/                          # Core business layer
│   └── src/main/java/
│       └── com/luketran/identity/domain/
│           ├── entities/            # Pure POJO entities (Account, App, AppRole...)
│           ├── enums/               # AuthFieldType, etc.
│           ├── exceptions/          # Business exceptions
│           └── repositories/        # Port interfaces (AccountRepository, etc.)
│
├── application/                     # Use case layer
│   └── src/main/java/
│       └── com/luketran/identity/application/
│           ├── services/            # AuthService, ScopeResolver, TokenService
│           ├── dto/                 # Request/Response DTOs
│           └── helpers/             # ScopeHelper, PasswordHelper
│
├── infrastructure/                  # Persistence & external adapters
│   └── src/main/
│       ├── java/com/luketran/identity/infrastructure/
│       │   ├── persistence/
│       │   │   ├── entities/        # JPA entities (@Entity, @Table)
│       │   │   ├── mappers/         # MapStruct mappers (Domain ↔ JPA)
│       │   │   ├── jpa/             # Spring Data JPA repositories
│       │   │   └── adapters/        # Port implementations
│       │   └── config/              # JPA/Flyway configuration
│       └── resources/
│           └── db/migration/        # Flyway SQL migrations
│
└── webapi/                          # Presentation & assembly
    └── src/main/
        ├── java/com/luketran/identity/webapi/
        │   ├── IdentityApplication.java     # @SpringBootApplication
        │   ├── config/                      # Security, JWT, OpenAPI, Cache config
        │   ├── security/                    # JwtFilter, DynamicKeyResolver
        │   └── controllers/                 # REST controllers
        └── resources/
            └── application.yaml             # Spring Boot configuration
```

---

## Database Schema

```
┌──────────────┐       ┌──────────────────┐
│   account    │──1:N──│   account_auth   │
│              │       │ (login methods)   │
│ - id (UUID)  │       │ - field_type      │
│ - name       │       │ - field_value     │
│ - secret_key │       └──────────────────┘
│ - password   │
│ - wrong_count│       ┌──────────────────┐
│              │──1:N──│   app_access     │──N:1──┐
└──────────────┘       │ - role_id        │       │
                       │ - scope          │       │
       ┌───────────────┘                  │       │
       │                                  │       │
       │  ┌──────────────────┐            │       │
       └──│ account_session  │            │       │
          │ (refresh token)  │            │       │
          │ - expired_at     │            │       │
          └──────────────────┘            │       │
                                          │       │
┌──────────────┐   ┌─────────────────┐   │       │
│   app_role   │─M:N│ app_permission  │   │       │
│ - code       │   │ - code           │   │       │
│ - name       │   │ - group_name     │   │       │
│ - app_id     │   │ - include_codes  │   │       │
└──────────────┘   └─────────────────┘   │       │
                                          │       │
                          ┌───────────────┘       │
                          │        app            │
                          │ - code (unique)       │
                          │ - signing_key (128ch) │
                          │ - token_lifetime      │
                          │ - session_lifetime    │
                          └───────────────────────┘
```

---

## Core Algorithm: Scope Resolution

The scope mechanism extends standard RBAC to support per-account permission overrides without role proliferation:

```
Input:  AppAccess { role: "manager", scope: "+export.data -order.delete" }
        Role "manager" permissions: [order.create, order.view, order.delete, order.report]
        Permission "order.create" includes: [order.view]

Algorithm:
  1. Parse scope string → positive: {export.data}, negative: {order.delete}
  2. Get role permissions → {order.create, order.view, order.delete, order.report}
  3. Merge: role_perms ∪ positive = {order.create, order.view, order.delete, order.report, export.data}
  4. Expand parent→children (transitive): order.create → adds order.view (already present)
  5. Apply negatives: remove order.delete

Output: "order.create order.view order.report export.data"
```

**Real-world use case:** Role "manager" has 50 permissions, but one specific account should NOT have delete access. Instead of creating a new role, simply set scope: `-order.delete`.

---

## How to Run

### Prerequisites
- Java 21+
- PostgreSQL (or Docker)
- Maven 3.9+

### Quick Start

```bash
# 1. Start PostgreSQL (if using Docker)
docker run -d --name pg-identity \
  -e POSTGRES_DB=identity \
  -e POSTGRES_PASSWORD=admin \
  -p 5432:5432 postgres:18

# 2. Run database migrations
mvn flyway:migrate -pl :infrastructure -Dflyway.password=admin

# 3. Build & run
mvn clean package -DskipTests
java -jar webapi/target/webapi-0.0.1-SNAPSHOT.jar

# Or run directly with Maven
mvn spring-boot:run -pl :webapi
```

### Access
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

### Default Credentials
| Account | Password | Scope |
|---------|----------|-------|
| root | root | admin (super permission) |

---

## API Overview

### Public Endpoints (no auth)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/login` | Login with username/email/phone + password |
| POST | `/auth/refresh` | Refresh access token |

### Protected Endpoints (Bearer token)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/me` | Current user profile |
| GET | `/me/permissions` | Resolved permission list |
| GET | `/auth/force-logout` | Check if force-logout required |

### Admin Endpoints (scope: "admin", issuer: "IDENTITY")
| Resource | Endpoints |
|----------|-----------|
| Apps | CRUD `/admin/apps` |
| Accounts | CRUD `/admin/accounts` + unlock, force-logout, set-password |
| Roles | CRUD `/admin/apps/{appId}/roles` + assign permissions |
| Permissions | CRUD `/admin/apps/{appId}/permissions` + tree view |
| Access | Grant/revoke `/admin/access` (account ↔ app binding) |

---

## Design Decisions

| Decision | Rationale |
|----------|-----------|
| **Scope override instead of more roles** | Avoids role explosion. In production systems with 100+ permissions, creating a role for every edge case is unmaintainable. |
| **Dynamic per-app signing key** | Security isolation between tenants. Key rotation for one app doesn't affect others. Cached 30min to minimize DB queries. |
| **Refresh token = session UUID (not JWT)** | Server-side revocation. Admin can force-logout a user instantly by deleting the session record. Stateless refresh tokens cannot be revoked until expiry. |
| **BCrypt (not MD5) for passwords** | Industry standard. Cost factor makes brute-force infeasible even with leaked hashes. Per-account salt built into BCrypt. |
| **Strict Clean Architecture (domain = pure POJO)** | Demonstrates understanding of dependency inversion. Domain logic is testable without Spring context. Infrastructure is swappable. |
| **MapStruct over ModelMapper** | Compile-time code generation = zero runtime reflection cost, type-safe, debuggable. |
| **Flyway over Hibernate ddl-auto** | Production-safe. Versioned, reviewable, rollback-friendly. `ddl-auto=validate` ensures entity/schema sync. |

---

## Testing Strategy

| Layer | Approach |
|-------|----------|
| Domain | Plain JUnit 5 — no Spring context needed |
| Application | Mockito (mock ports) — fast, isolated |
| Infrastructure | `@DataJpaTest` + Testcontainers (real PostgreSQL) |
| WebAPI | `@SpringBootTest` + MockMvc — full integration |

### Critical Test Cases
- `ScopeResolverTest` — positive/negative override, parent-child expansion, empty scope
- `TokenServiceTest` — correct claims, wrong key rejection, expiry validation
- `AuthServiceTest` — login flow, brute force lockout, refresh token lifecycle

---

## Build Commands

```bash
mvn clean compile                    # Compile all modules
mvn clean package -DskipTests        # Build JAR
mvn test                             # Run all tests
mvn flyway:migrate -pl :infrastructure -Dflyway.password=xxx  # Apply migrations
mvn dependency:tree -pl domain       # Verify domain has zero framework deps
```

---

## Skills Demonstrated

- **System Design** — multi-tenant IAM architecture serving multiple applications
- **Clean Architecture** — strict layer separation, dependency inversion, port/adapter pattern
- **Spring Security** — custom JWT filter, dynamic key resolution, programmatic authorization
- **Authorization Design** — RBAC + scope override + hierarchical permissions (novel approach)
- **Database Design** — normalized schema, junction tables, soft-delete pattern, proper indexing
- **API Design** — RESTful resource modeling, pagination, consistent error format
- **Testing** — unit, integration, and end-to-end with Testcontainers
- **DevOps** — Docker, Flyway migrations, environment-based configuration
- **Java Ecosystem** — Maven multi-module, MapStruct, Lombok, Caffeine cache

---

## References

This project is a Java rebuild of a production .NET IAM system, redesigned with:
- Simplified feature set (removed site-level permissions, OAuth providers, Hangfire jobs)
- Modern Java idioms (records, sealed classes, pattern matching)
- Strict Clean Architecture (original used a more pragmatic layering)
- BCrypt instead of MD5 for password hashing (security improvement)
