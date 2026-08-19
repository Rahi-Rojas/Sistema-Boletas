# AGENTS.md

## Project overview

Spring Boot 3.4.1 / Java 17 REST API for product, order, and user management. MySQL backend, JWT auth, local `uploads/` for image files. App runs on **port 8081** (not 8080).

## Build & run

```bash
# Build (skip tests for speed)
./mvnw clean package -DskipTests

# Run locally
./mvnw spring-boot:run
```

Local DB is `tienda_rojas` on `localhost:3306`, user `root`, password `mysql` (defaults in `application.properties`, overridable via `SPRING_DATASOURCE_*` env vars). Hibernate creates/updates the schema automatically (`ddl-auto=update`) — no migrations. The app fails to start if MySQL is unreachable.

`JWT_SECRET` must be ≥32 chars (validated in `JwtService` constructor). Dev fallback is set in `application.properties`; the real secret lives in `.env` (gitignored).

## Testing

- Most tests are **pure Mockito + JUnit 5 unit tests** — no Spring context, no DB.
- **Exception:** `ProductosApplicationTests.contextLoads` is a `@SpringBootTest` that boots the full context and **requires a live MySQL**. So `./mvnw test` fails if the DB is down.
- Run only the unit tests (DB-independent) with:
  ```bash
  ./mvnw test -Dtest=AuthControllerTest,OrderServiceTest,ProductServiceTest,UserServiceTest
  ```
- Conventions: mock repos/mappers/services with `@Mock` / `@InjectMocks`; response objects are **Java records** (use `.field()`, not getters); Spanish method names like `cuandoCrearProducto_entoncesRetornarProductResponse`.

## Architecture

```
Controller -> Service (interface) -> ServiceImpl -> Repository (Spring Data JPA)
                                          |
                                      Mapper (MapStruct)
```

- **Mappers**: MapStruct with `@Mapper(componentModel = "spring")`. Edit the MapStruct interface for entity↔DTO mapping, never manual mapping code. MapStruct emits harmless "unmapped target property" warnings at compile time.
- **Soft delete**: Products use an `isActive` flag. `DELETE /api/v1/product/delete-soft/{id}` sets `isActive=false`; `delete-hard/{id}` removes the row. Admin users are protected from soft-delete in `UserServiceImpl.deleteById`.
- **Generic service layer**: `BaseGenericService<RQ, RS, ID>` / `BaseGenericServiceImpl` provide shared CRUD; extend for new entities.

## API conventions

- Base path: `/api/v1/`
- Product create/update/patch accept **multipart/form-data**:
  - `request` — JSON string via `@RequestPart` (not a JSON body); parsed with `ObjectMapper` in the controller
  - `file` — image `MultipartFile` (required on create, optional on update/patch)
- Auth: `POST /api/v1/auth/**` and GET on `/api/v1/product/**` are public; other endpoints need a JWT `Bearer` token.

## Security

- Stateless JWT. Roles are plain **authorities** (no `ROLE_` prefix) — e.g. `hasAuthority("ADMIN")`. `GrantedAuthorityDefaults("")` and `@EnableMethodSecurity` are set; admin routes use `@PreAuthorize("hasAuthority('ADMIN')")` on `UserController` (all mutations) and `OrderController` (`find-all`, `reporte-ventas`).
- **Order ownership**: `OrderServiceImpl` enforces object-level authorization (IDOR protection). `create`, `findById` and `cancelOrder` only operate on the authenticated user's own orders unless the caller has `ADMIN` authority. `create` overrides `OrderRequest.userId` to the current user for non-admins; `findById`/`cancelOrder` return 403 `ApiErrorException` for other users' orders. `OrderServiceTest` sets up `SecurityContextHolder` in `@BeforeEach`/`loginAs(...)` helper.
- **Rate limiting**: `RateLimitFilter` (bucket4j `8.10.1`) limits `/api/v1/auth/**` to 10 req/min per IP. Register any limit tweaks there; do NOT downgrade bucket4j, use the builder API (`Bandwidth.builder().capacity(...).refillGreedy(...)`), not the deprecated `Bandwidth.classic`.
- **GlobalExceptionHandler**: 500s return generic `"Error interno del servidor"` and log server-side — never `ex.getMessage()` in the catch-all. `RuntimeException`/`ApiErrorException` keep their messages (business errors). `ConstraintViolationException` maps to per-field errors (used by ProductController validation).
- **Production profile**: `application-prod.properties` (activate with `SPRING_PROFILES_ACTIVE=prod`) disables `show-sql`, requires real `JWT_SECRET` (no dev fallback), and restricts CORS via `APP_CORS_ORIGINS`. CORS origins are read from `app.cors.allowed-origins` (default `*` in dev) in `SecurityConfig`.
- CORS is wide-open (`*` origin) — dev only; lock down before production.
- `docker-compose.yml` contains a **hardcoded Railway MySQL URL + credentials**, and `.env` holds the same DB password plus a JWT secret (gitignored). Do not commit new secrets.

## Gotchas

- Product image URLs are built from `app.upload.url-base` (property in `application.properties`) — not hardcoded in `ProductServiceImpl` anymore.
- `UploadFileService` **hardcodes** the relative path `"uploads"`; the `ruta.subida.imagenes` property in `application.properties` is unused dead config. Docker maps `./uploads:/app/uploads`.
- `ProductController` receives product JSON as `@RequestPart("request") String` — it's parsed and validated manually with the injected `Validator` (plain `@Valid` won't work on a raw String). Don't revert to `new ObjectMapper()` per request.
- `MapStruct` + `Lombok` need `mapstruct-processor` with `provided` scope — already configured.
- `spring.jpa.show-sql=true` is on; disable for prod.
- `pom.xml` duplicates (`s3`, `jjwt`) were removed — don't re-add them. `jjwt-impl`/`jjwt-jackson` should stay `runtime` scope.
