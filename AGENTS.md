# AGENTS.md

## Project overview

Spring Boot 3.4.1 / Java 17 REST API for product, order, and user management. MySQL backend, JWT auth, AWS S3 for image uploads.

## Build & run

```bash
# Build (skip tests for speed)
./mvnw clean package -DskipTests

# Run locally
./mvnw spring-boot:run

# Run tests (unit only, no DB needed)
./mvnw test
```

App starts on **port 8081** (not 8080).

## Testing

Tests are **pure unit tests** with Mockito + JUnit 5 — no Spring context, no database. When adding tests:
- Mock repositories, mappers, and services with `@Mock` / `@InjectMocks`.
- Response objects are **Java records** — use `.field()` accessors, not getters.
- Test methods use Spanish naming convention (e.g. `cuandoCrearProducto_entoncesRetornarProductResponse`).

## Architecture

```
Controller -> Service (interface) -> ServiceImpl -> Repository (Spring Data JPA)
                                          |
                                      Mapper (MapStruct)
```

- **Mappers**: MapStruct with `@Mapper(componentModel = "spring")`. When modifying entity-to-DTO mapping, edit the MapStruct interface — not manual mapping code.
- **Soft delete**: Products use an `isActive` flag. `delete-soft/{id}` sets `isActive = false`; `delete-hard/{id}` physically removes the row.
- **Generic service layer**: `BaseGenericService<RQ, RS, ID>` / `BaseGenericServiceImpl` provide shared CRUD. Extend these for new entities.

## API conventions

- Base path: `/api/v1/`
- Product create/update endpoints accept **multipart/form-data** with two parts:
  - `request` — JSON string (not a JSON body)
  - `file` — image `MultipartFile`
- Auth: `POST /api/v1/auth/**` is public; most other endpoints require JWT `Bearer` token.
- GET on `/api/v1/product/**` is public (no auth needed).

## Security

- Stateless JWT sessions. Roles use **authorities** (not role prefixes) — e.g. `hasAuthority("ADMIN")`.
- CORS is wide-open (`*` origin) — intended for development. Lock down before production.
- `application.properties` and `docker-compose.yml` contain a **hardcoded Railway MySQL connection string with credentials**. Do not commit new secrets.

## Gotchas

- `pom.xml` has **duplicate dependencies**: `s3` and `jjwt` appear twice. Clean up if you touch this file.
- `MapStruct` + `Lombok` require `mapstruct-processor` with `provided` scope — already configured, but adding new mappers needs annotation processing.
- `spring.jpa.hibernate.ddl-auto=update` — schema is auto-managed by Hibernate. No Flyway/Liquibase migrations.
- `uploads/` directory is used for local file storage; mapped into Docker container at `/app/uploads`.
