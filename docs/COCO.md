# Identity Service - Project Rules

## Architecture: Strict Clean Architecture (Java Spring Boot, Multi-Module Maven)

### Module Structure

- **domain/** - Pure POJOs: entities, enums, value objects, domain exceptions, repository interfaces (ports). ZERO framework dependency (only Lombok).
- **application/** - Use case services, DTOs (request/response), helpers. Depends only on domain.
- **infrastructure/** - JPA entity classes, MapStruct mappers (Domain ↔ JPA), repository adapters (implements domain ports), Flyway migrations. Depends on domain.
- **webapi/** - REST controllers, Spring Security config, JWT filter, Spring Boot entry point. Assembly module (builds executable JAR). Depends on application + infrastructure.
- Base package: `com.luketran.identity`
- Module-specific sub-packages: `com.luketran.identity.domain`, `com.luketran.identity.application`, `com.luketran.identity.infrastructure`, `com.luketran.identity.webapi`

### Dependency Rules

- domain depends on NOTHING (pure Java + Lombok only)
- application depends on domain only
- infrastructure depends on domain (implements port interfaces)
- webapi depends on application + infrastructure (assembly point)
- NEVER import from a higher layer (e.g., domain must never import from application)

### Domain Entity vs JPA Entity (Critical)

- **Domain entities** (in `domain/entities/`) are plain Java classes (POJO) with NO JPA annotations
- **JPA entities** (in `infrastructure/persistence/entities/`) have `@Entity`, `@Table`, `@Column` — these map to the database
- **MapStruct mappers** (in `infrastructure/persistence/mappers/`) convert between the two
- **Repository adapters** (in `infrastructure/persistence/adapters/`) implement domain port interfaces using JPA repositories

### Package Conventions

- One class per file
- Package names: lowercase, no underscores in package segments
- Class placement must respect layer boundaries

---

## Database Migrations: Flyway

### File Location

All migration SQL files go in: `infrastructure/src/main/resources/db/migration/`

Run migrations from project root:
```bash
mvn flyway:migrate -pl :infrastructure -Dflyway.password=xxx
```

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

### Domain Entity Classes (domain layer)

- Pure POJOs with Lombok annotations only (`@Getter`, `@Setter`, `@Builder`, etc.)
- NO JPA annotations (`@Entity`, `@Table`, `@Column` are FORBIDDEN here)
- NO framework imports (no Spring, no Jakarta Persistence)
- Use Java types: `UUID`, `String`, `LocalDateTime`, enums
- Business logic methods belong here
- Use a base class with common fields (id, createdAt, deletedAt)

### JPA Entity Classes (infrastructure layer)

- Located in `infrastructure/persistence/entities/`
- Annotate with `@Entity`, `@Table(name = "table_name")`
- Use `@Id` + `@GeneratedValue(strategy = GenerationType.UUID)` for primary key
- Use `@Column(name = "column_name")` to match snake_case DB columns
- Soft delete: `@Column(name = "deleted_at") private LocalDateTime deletedAt`
- Relationships: prefer `FetchType.LAZY`
- Use `@MappedSuperclass` for base JPA entity with common fields
- Naming: `{Domain}JpaEntity` (e.g., `AccountJpaEntity`)

### MapStruct Mappers (infrastructure layer)

- Located in `infrastructure/persistence/mappers/`
- Annotate with `@Mapper(componentModel = "spring")`
- Naming: `{Domain}PersistenceMapper` (e.g., `AccountPersistenceMapper`)
- Methods: `toDomain(JpaEntity)` and `toJpaEntity(DomainEntity)`
- MapStruct processor + lombok-mapstruct-binding configured in parent POM

### Repository Interfaces — Ports (domain layer)

- Located in `domain/repositories/`
- Plain Java interfaces (no Spring annotations)
- Methods return domain entities, not JPA entities
- Naming: `{Domain}Repository` (e.g., `AccountRepository`)

### Repository Adapters (infrastructure layer)

- Located in `infrastructure/persistence/adapters/`
- Annotate with `@Repository` (Spring component)
- Implements domain port interface
- Delegates to JPA repository + uses MapStruct mapper for conversion
- Naming: `{Domain}RepositoryAdapter` (e.g., `AccountRepositoryAdapter`)

### JPA Repositories (infrastructure layer)

- Located in `infrastructure/persistence/jpa/`
- Extend `JpaRepository<JpaEntity, UUID>`
- Custom queries: use `@Query` with JPQL or native SQL
- Naming: `{Domain}JpaRepository` (e.g., `AccountJpaRepository`)
- These are internal to infrastructure, NOT exposed to other layers

### Service Classes (application layer)

- Annotate with `@Service`
- Naming: `{Domain}Service` (e.g., `AuthService`, `AccountService`)
- Use `@Transactional` on methods that write data
- Use `@Transactional(readOnly = true)` on read-only methods
- Throw domain-specific exceptions, not generic Spring exceptions
- Prefer `Optional` over null returns
- Depend on domain repository interfaces (ports), not JPA repositories directly

### DTOs (application layer)

- Request DTOs: `{Action}{Domain}Request` (e.g., `LoginRequest`, `CreateAccountRequest`)
- Response DTOs: `{Domain}Response` or `{Domain}DetailResponse`
- Use Jakarta validation annotations (`@NotBlank`, `@Size`, `@Email`, `@NotNull`)
- DTOs are records or Lombok `@Data` classes - never entities
- Place in `application/dto/request` and `application/dto/response` sub-packages
- **Swagger Documentation**: Annotate DTOs with `@Schema`. Include `description`, realistic `example` values, and `requiredMode` mapping to Jakarta validation constraints (e.g., `Schema.RequiredMode.REQUIRED`).
- **Sensitive Fields**: Hide internal or sensitive fields from API documentation using `@Schema(hidden = true)`.

### REST Controllers (webapi layer)

- Annotate with `@RestController` + `@RequestMapping("/api/v1/...")`
- Return `ResponseEntity<ApiResponse<T>>`
- HTTP methods: GET (read), POST (create), PUT (full update), PATCH (partial update), DELETE (soft delete)
- Admin endpoints: `/api/v1/admin/...`
- Public auth endpoints: `/api/v1/auth/...`
- Authenticated user endpoints: `/api/v1/me/...`
- Use `@Valid` on request body parameters
- **API Documentation (Swagger/OpenAPI 3)**:
  - Class level: Annotate with `@Tag` to categorize endpoints.
  - Method level: Annotate with `@Operation` (summary, description).
  - Error documenting: Annotate with `@io.swagger.v3.oas.annotations.responses.ApiResponse` (use fully qualified name to avoid classname collision with DTO `ApiResponse`) inside `@ApiResponses` to cover all possible HTTP statuses (200, 400, 401, 403, 429, 500) and link error DTO schema (`ApiResponse.class`).
  - Security mapping: Use `@SecurityRequirement(name = "bearerAuth")` on all secured endpoints.
  - Internal endpoints: Annotate with `@Hidden` to hide helper or testing endpoints from Swagger UI.

### Security (webapi layer)

- Spring Security 6 filter chain configuration (SecurityFilterChain bean)
- JWT validation via custom `OncePerRequestFilter`
- BCrypt for password hashing (NEVER MD5)
- Custom `@RequireScope` annotation for permission checks
- Stateless sessions (`SessionCreationPolicy.STATELESS`)

### Error Handling (webapi layer)

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
- Flyway auto-migrates the test database
- Test file location mirrors main source structure under `src/test/java/`
- Domain tests need NO Spring context (pure POJO)
- Infrastructure tests use `@DataJpaTest` + Testcontainers

---

## Code Quality Rules

- All public API methods must validate input (fail fast with proper error messages)
- Use `@Transactional` on service methods that write data
- Log at appropriate levels: ERROR (failures), WARN (recoverable), INFO (key operations), DEBUG (detail)
- Prefer composition over inheritance
- Keep methods short (under 30 lines ideally)
- Use meaningful variable and method names - code should be self-documenting
- Configuration values belong in `application.yaml` (webapi module), not hardcoded

---

## Project-Specific Decisions

- Database: PostgreSQL (fixed, no other DB support needed)
- Migration tool: Flyway (not Liquibase)
- Password hashing: BCrypt via Spring Security's `PasswordEncoder`
- JWT library: JJWT (io.jsonwebtoken)
- Entity mapping: MapStruct (compile-time, not runtime reflection)
- Cache: Caffeine (in-memory)
- API docs: SpringDoc OpenAPI (Swagger UI at `/swagger-ui.html`)
- No OAuth providers (scope limited to password-based auth)
- Soft delete pattern: `deleted_at` column, filter with `WHERE deleted_at IS NULL`
- `application.yaml` lives in webapi module (only the runnable module needs Spring config)
- Flyway plugin configured in infrastructure module POM
