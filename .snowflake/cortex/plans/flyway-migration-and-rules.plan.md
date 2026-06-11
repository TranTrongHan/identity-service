---
name: "flyway-migration-and-rules"
created: "2026-06-11T10:05:45.497Z"
status: pending
---

# Plan: Replace Liquibase with Flyway + Add Project Rules

## Context

### Key Findings

- Project is **early-stage Spring Boot 3.5.14** with Java 21
- Current dependency: `org.liquibase:liquibase-core` (no migrations written yet)
- Existing empty directory: `src/main/resources/db/changelog/` (Liquibase convention)
- `application.yaml` has `spring.liquibase.enabled: false` (Liquibase was disabled anyway)
- No `COCO.md` or `.snowflake/cortex/` rules exist yet
- Architecture follows Clean Architecture with an `infracstructure` package

### Flyway + Clean Architecture (Answering Question 2)

Spring Boot auto-configures Flyway to scan `classpath:db/migration`. This path lives in `src/main/resources/db/migration/` which is always on the classpath regardless of your Java package structure. Your Clean Architecture layers (`domain`, `application`, `infrastructure`) are Java code packages - they don't affect where resource files (SQL migrations) are resolved from.

**TL;DR:** Keep migrations in `src/main/resources/db/migration/` (the standard). Spring will find them automatically. Your `infrastructure` Java package holds JPA entities, repositories, and DB config classes - not migration SQL files.

```
src/main/
├── java/com/luketran/identity/identity_service/
│   ├── domain/           (entities, enums, exceptions)
│   ├── application/      (services, DTOs, use cases)
│   └── infracstructure/  (JPA repos, configs, adapters)
└── resources/
    └── db/migration/     <-- Flyway reads from HERE (classpath-based, not package-based)
        ├── V1__init_schema.sql
        ├── V2__seed_data.sql
        └── V3__indexes.sql
```

---

## Implementation Steps

### Step 1: Update pom.xml - Replace Liquibase with Flyway

**File:** identity-service/pom.xml

Remove:

```xml
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-core</artifactId>
</dependency>
```

Add:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

Note: Since Spring Boot 3.5.14 manages Flyway versions via the BOM, no explicit `<version>` is needed. The `flyway-database-postgresql` module is **required** since Flyway 10+ modularized database support.

### Step 2: Restructure Migration Directory

- Delete: `src/main/resources/db/changelog/` (empty Liquibase directory)
- Create: `src/main/resources/db/migration/` (Flyway default location)

### Step 3: Update application.yaml

**File:** `identity-service/src/main/resources/application.yaml`

Replace:

```yaml
spring:
  liquibase:
    enabled: false
```

With:

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: '0'
```

`baseline-on-migrate: true` ensures Flyway works cleanly on an existing database without failing on the first run. `baseline-version: '0'` means all V1+ migrations will be applied.

### Step 4: Create COCO.md Project Rules

**File:** `d:/Microservices/identity-service/COCO.md`

This file will contain project-wide rules for Cortex Code covering:

```markdown
# Identity Service - Project Rules

## Architecture: Clean Architecture (Java Spring Boot)

### Layer Structure
- **domain/** - Entities, enums, value objects, domain exceptions, repository interfaces. NO framework annotations except JPA entity annotations.
- **application/** - Use case services, DTOs (request/response), mappers. Depends only on domain.
- **infracstructure/** - JPA repositories, Spring configs, external adapters. Implements domain interfaces.
- Base package: `com.luketran.identity.identity_service`

### Dependency Rules
- domain depends on NOTHING (pure Java)
- application depends on domain only
- infracstructure depends on domain (implements interfaces)
- The Spring Boot main class scans all sub-packages automatically

---

## Database Migrations: Flyway

### File Location
- All migration SQL files go in: `src/main/resources/db/migration/`
- Spring Boot auto-detects this location via `classpath:db/migration`

### Naming Convention (STRICT)
- Versioned: `V{version}__{description}.sql` (double underscore between version and description)
- Repeatable: `R__{description}.sql`
- Version format: sequential integers `V1`, `V2`, `V3` (not timestamps, not decimals)
- Description: lowercase_snake_case, descriptive of what the migration does
- Examples:
  - `V1__init_schema.sql`
  - `V2__seed_data.sql`
  - `V3__add_indexes.sql`
  - `V4__add_column_avatar_url_to_account.sql`
  - `R__refresh_views.sql` (repeatable, re-run on change)

### Migration Content Rules
- Each migration must be idempotent where possible (use `IF NOT EXISTS`)
- Always specify `NOT NULL` constraints explicitly
- Use `gen_random_uuid()` for UUID defaults (PostgreSQL native)
- Use `TIMESTAMP` (without timezone) for all datetime columns, store UTC
- Include `ON DELETE` and `ON UPDATE` behavior for foreign keys
- One logical change per migration (don't mix unrelated DDL)
- NEVER modify or delete an existing migration file that has been applied
- To undo a migration, create a NEW migration that reverses the change
- Write `DROP TABLE IF EXISTS` or `DROP INDEX IF EXISTS` in down scenarios

### Schema Conventions
- Table names: lowercase_snake_case, singular (`account`, NOT `accounts`)
- Column names: lowercase_snake_case
- Primary key: `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- Audit columns: `created_at TIMESTAMP NOT NULL DEFAULT NOW()`, `deleted_at TIMESTAMP`
- Foreign keys: `{referenced_table}_id` (e.g., `account_id`, `app_id`)
- Indexes: `idx_{table}_{column(s)}` (e.g., `idx_account_auth_field_type_value`)
- Unique constraints: `uq_{table}_{column(s)}`

---

## Java / Spring Boot Conventions

### General
- Java 21, Spring Boot 3.5.x
- Use Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Use `@RequiredArgsConstructor` for constructor injection (preferred over `@Autowired`)
- Never use field injection (`@Autowired` on fields)

### Entity Classes (domain layer)
- Annotate with `@Entity`, `@Table(name = "table_name")`
- Use `@Id` + `@GeneratedValue(strategy = GenerationType.UUID)` for primary key
- Use `@Column(name = "column_name")` to match snake_case DB columns
- Soft delete: use `@Column(name = "deleted_at") private LocalDateTime deletedAt`
- Relationships: prefer lazy loading (`FetchType.LAZY`)

### Repository Interfaces (infracstructure layer)
- Extend `JpaRepository<Entity, UUID>`
- Custom queries: use `@Query` with JPQL or native SQL
- Naming: `{Entity}Repository`

### Service Classes (application layer)
- Annotate with `@Service`
- Naming: `{Domain}Service` (e.g., `AuthService`, `AccountService`)
- Use interface + implementation pattern for testability
- Throw domain exceptions, not Spring exceptions

### DTOs (application layer)
- Request DTOs: `{Action}{Domain}Request` (e.g., `LoginRequest`, `CreateAccountRequest`)
- Response DTOs: `{Domain}Response` or `{Domain}DetailResponse`
- Use Jakarta validation annotations (`@NotBlank`, `@Size`, `@Email`)

### REST Controllers
- Annotate with `@RestController` + `@RequestMapping("/api/v1/...")`
- Return `ResponseEntity<ApiResponse<T>>`
- HTTP methods: GET (read), POST (create), PUT (update), DELETE (soft delete)
- Admin endpoints under `/api/v1/admin/...`
- Public endpoints under `/api/v1/auth/...`

### Security
- Use Spring Security 6 filter chain configuration
- JWT validation via custom `OncePerRequestFilter`
- BCrypt for password hashing (NOT MD5)
- Custom `@RequireScope` annotation for permission checks

### Testing
- Unit tests: JUnit 5 + Mockito
- Integration tests: `@SpringBootTest` + Testcontainers (PostgreSQL)
- Test class naming: `{Class}Test` for unit, `{Class}IntegrationTest` for integration
- Migration tests: rely on Flyway auto-migrating test database

---

## Code Quality
- No wildcard imports (`import java.util.*` is forbidden)
- No unused imports
- No empty catch blocks
- All public API methods must validate input (fail fast)
- Prefer `Optional` over null returns from repository methods
- Use `@Transactional` on service methods that write data
- Log at appropriate levels: ERROR (failures), WARN (recoverable), INFO (key operations), DEBUG (detail)
```

---

## Verification

After implementation, verify with:

1. **Maven compile check:**

   ```bash
   cd identity-service/identity-service
   ./mvnw compile
   ```

   Should compile without errors (Flyway on classpath, Liquibase removed).

2. **Flyway auto-configuration:** Start the app - Spring Boot should log:

   ```
   Flyway Community Edition ...
   Successfully validated X migration(s)
   ```

   (Or "no migrations found" if db/migration is empty, which is fine at this stage)

3. **Verify Liquibase is gone:**

   ```bash
   ./mvnw dependency:tree | grep liquibase
   ```

   Should return nothing.

4. **Verify Flyway is present:**

   ```bash
   ./mvnw dependency:tree | grep flyway
   ```

   Should show `flyway-core` and `flyway-database-postgresql`.

---

## Critical Files

- identity-service/identity-service/pom.xml - Replace liquibase-core with flyway dependencies
- identity-service/identity-service/src/main/resources/application.yaml - Update config from liquibase to flyway
- `COCO.md` (to create at project root) - Project rules including Flyway conventions
- `identity-service/src/main/resources/db/migration/` (to create) - Flyway migration directory
