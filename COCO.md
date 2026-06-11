# Identity Service - Project Rules

## Architecture: Clean Architecture (Java Spring Boot)

### Layer Structure

- **domain/** - Entities, enums, value objects, domain exceptions, repository interfaces. NO framework dependencies except JPA entity annotations.
- **application/** - Use case services, DTOs (request/response), mappers. Depends only on domain.
- **infrastructure/** - JPA repository implementations, Spring configurations, security filters, external adapters. Implements domain interfaces.
- **presentation/** - REST controllers, request/response handling, API documentation annotations. Thin layer that delegates to application services.
- Base package: `com.luketran.identity.identity_service`

### Dependency Rules

- domain depends on NOTHING (pure Java + JPA annotations only)
- application depends on domain only
- infrastructure depends on domain (implements interfaces)
- presentation depends on application (calls services, uses DTOs)
- The Spring Boot main class scans all sub-packages automatically via `@SpringBootApplication`

### Package Conventions

- One class per file
- Package names: lowercase, no underscores in package segments
- Class placement must respect layer boundaries - never import from a higher layer

---

## Database Migrations: Flyway

### File Location

All migration SQL files go in: `src/main/resources/db/migration/`

Spring Boot auto-detects this location via `classpath:db/migration`. Do NOT place migration files inside Java packages.

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
- Include `ON DELETE` and `ON UPDATE` behavior for foreign keys when not using default (RESTRICT)
- One logical change per migration (don't mix unrelated DDL)
- NEVER modify or delete an existing migration file that has been applied
- To undo a change, create a NEW migration that reverses it
- Use `IF EXISTS` in DROP statements (`DROP TABLE IF EXISTS`, `DROP INDEX IF EXISTS`)
- Add comments at the top of each migration describing the purpose

### Schema Conventions

- Table names: lowercase_snake_case, singular (`account`, NOT `accounts`)
- Column names: lowercase_snake_case
- Primary key: `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- Audit columns: `created_at TIMESTAMP NOT NULL DEFAULT NOW()`, `deleted_at TIMESTAMP`
- Foreign keys: `{referenced_table}_id` (e.g., `account_id`, `app_id`)
- Index naming: `idx_{table}_{column(s)}` (e.g., `idx_account_auth_field_type_value`)
- Unique constraint naming: `uq_{table}_{column(s)}`
- Foreign key naming: `fk_{table}_{referenced_table}`

### Migration Example Template

```sql
-- V{N}__description_here.sql
-- Purpose: Brief description of what this migration does

CREATE TABLE IF NOT EXISTS table_name (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    reference_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    CONSTRAINT fk_table_reference FOREIGN KEY (reference_id) REFERENCES other_table(id)
);

CREATE INDEX IF NOT EXISTS idx_table_reference_id ON table_name(reference_id);
```

---

## Java / Spring Boot Conventions

### General

- Java 21, Spring Boot 3.5.x
- Use Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Getter`, `@Setter`
- Use `@RequiredArgsConstructor` for constructor injection (preferred over `@Autowired`)
- Never use field injection (`@Autowired` on fields)
- No wildcard imports (`import java.util.*` is forbidden)
- No unused imports
- No empty catch blocks

### Entity Classes (domain layer)

- Annotate with `@Entity`, `@Table(name = "table_name")`
- Use `@Id` + `@GeneratedValue(strategy = GenerationType.UUID)` for primary key
- Use `@Column(name = "column_name")` to match snake_case DB columns
- Soft delete: `@Column(name = "deleted_at") private LocalDateTime deletedAt`
- Relationships: prefer `FetchType.LAZY`
- Use `@MappedSuperclass` for base entity with common fields (id, createdAt, deletedAt)
- Temporal fields use `LocalDateTime` (Java) mapped to `TIMESTAMP` (PostgreSQL)

### Repository Interfaces (infracstructure layer)

- Extend `JpaRepository<Entity, UUID>`
- Custom queries: use `@Query` with JPQL or native SQL
- Naming: `{Entity}Repository`
- Place in `infracstructure.persistence` sub-package

### Service Classes (application layer)

- Annotate with `@Service`
- Naming: `{Domain}Service` (e.g., `AuthService`, `AccountService`)
- Use `@Transactional` on methods that write data
- Use `@Transactional(readOnly = true)` on read-only methods
- Throw domain-specific exceptions, not generic Spring exceptions
- Prefer `Optional` over null returns

### DTOs (application layer)

- Request DTOs: `{Action}{Domain}Request` (e.g., `LoginRequest`, `CreateAccountRequest`)
- Response DTOs: `{Domain}Response` or `{Domain}DetailResponse`
- Use Jakarta validation annotations (`@NotBlank`, `@Size`, `@Email`, `@NotNull`)
- DTOs are records or Lombok `@Data` classes - never entities
- Place in `application.dto.request` and `application.dto.response` sub-packages

### REST Controllers (infracstructure layer)

- Annotate with `@RestController` + `@RequestMapping("/api/v1/...")`
- Return `ResponseEntity<ApiResponse<T>>`
- HTTP methods: GET (read), POST (create), PUT (full update), PATCH (partial update), DELETE (soft delete)
- Admin endpoints: `/api/v1/admin/...`
- Public auth endpoints: `/api/v1/auth/...`
- Authenticated user endpoints: `/api/v1/me/...`
- Use `@Valid` on request body parameters

### Security

- Spring Security 6 filter chain configuration (SecurityFilterChain bean)
- JWT validation via custom `OncePerRequestFilter`
- BCrypt for password hashing (NEVER MD5)
- Custom `@RequireScope` annotation for permission checks
- Stateless sessions (`SessionCreationPolicy.STATELESS`)

### Error Handling

- `@RestControllerAdvice` global exception handler
- Consistent response format: `{"success": bool, "data": T, "error": {...}}`
- Map exceptions to proper HTTP status codes:
  - 400: validation errors
  - 401: authentication failure
  - 403: authorization failure
  - 404: resource not found
  - 429: brute force / rate limit
  - 500: unexpected errors

### Testing

- Unit tests: JUnit 5 + Mockito
- Integration tests: `@SpringBootTest` + Testcontainers (PostgreSQL)
- Test class naming: `{Class}Test` for unit, `{Class}IntegrationTest` for integration
- Flyway auto-migrates the test database (no separate test migration config needed)
- Test file location mirrors main source structure under `src/test/java/`

---

## Code Quality Rules

- All public API methods must validate input (fail fast with proper error messages)
- Use `@Transactional` on service methods that write data
- Log at appropriate levels: ERROR (failures), WARN (recoverable), INFO (key operations), DEBUG (detail)
- Prefer composition over inheritance
- Keep methods short (under 30 lines ideally)
- Use meaningful variable and method names - code should be self-documenting
- Configuration values belong in `application.yaml`, not hardcoded

---

## Project-Specific Decisions

- Database: PostgreSQL (fixed, no other DB support needed)
- Migration tool: Flyway (not Liquibase)
- Password hashing: BCrypt via Spring Security's `PasswordEncoder`
- JWT library: JJWT (io.jsonwebtoken)
- Cache: Caffeine (in-memory)
- API docs: SpringDoc OpenAPI (Swagger UI at `/swagger-ui.html`)
- No OAuth providers (scope limited to password-based auth)
- Soft delete pattern: `deleted_at` column, filter with `WHERE deleted_at IS NULL`
