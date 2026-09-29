# AGENTS.md

## Project purpose

This repository is an educational full-stack application for learning Rust backend development, PostgreSQL, Kotlin, and Compose Multiplatform. Keep the code approachable and evolve it incrementally toward production-quality practices.

The application manages users through a CRUD API:

- The backend is written in Rust with Axum, Tokio, SQLx, Serde, Utoipa, and PostgreSQL.
- The client is written with Kotlin Multiplatform and Compose Multiplatform for Android and iOS.
- PostgreSQL runs locally in Docker.
- The mobile client communicates with the backend through JSON over HTTP.

Do not introduce distributed systems, microservices, event buses, or other infrastructure unless a concrete requirement justifies them. Prefer the smallest change that teaches or demonstrates a professional practice.

## Repository layout

```text
FullStackUsersApp/
├── AGENTS.md
├── axum-server/       # Rust/Axum backend
└── HandleUsers/       # Kotlin/Compose Multiplatform client
```

Important backend paths:

```text
axum-server/src/domain/          # Entities, domain errors, repository ports
axum-server/src/application/     # Use cases and business rules
axum-server/src/infrastructure/  # PostgreSQL and in-memory adapters
axum-server/src/presentation/    # Axum handlers, routes, and OpenAPI
axum-server/tests/               # Backend tests
```

Important client paths:

```text
HandleUsers/shared/src/commonMain/   # Shared UI, state, models, and networking
HandleUsers/shared/src/androidMain/  # Android-specific shared implementation
HandleUsers/shared/src/iosMain/      # iOS-specific shared implementation
HandleUsers/androidApp/              # Android entry point
HandleUsers/iosApp/                  # iOS entry point
```

Generated directories such as `target/`, `build/`, `.gradle/`, and `.kotlin/` are not source code and must not be edited manually.

## Current behavior

The API currently exposes:

```text
GET    /api/v1/users
GET    /api/v1/users/{id}
POST   /api/v1/users
PUT    /api/v1/users/{id}
DELETE /api/v1/users/{id}
GET    /swagger-ui/
```

The client can:

- List users.
- Search and filter the locally loaded list.
- Create and edit users.
- Activate or deactivate users.
- Delete users after confirmation.
- Configure and test the backend URL.

The default backend address is `127.0.0.1:3000`. The Android emulator reaches the host backend through `http://10.0.2.2:3000`.

## General working rules

1. Inspect the relevant code before changing it.
2. Preserve the separation between domain, application, infrastructure, and presentation.
3. Make changes in small, reviewable increments.
4. Avoid unrelated refactors while implementing a feature or fix.
5. Preserve existing user changes and do not replace working code without a reason.
6. Prefer explicit, readable code over abstractions created for hypothetical future needs.
7. Update tests and documentation when behavior, configuration, commands, or API contracts change.
8. Never commit secrets, local database credentials, `local.properties`, IDE state, or generated build artifacts.
9. Do not claim that a command, platform, or integration works unless it was verified or clearly mark it as unverified.
10. Keep user-facing text in Spanish unless the product language is deliberately changed. Code identifiers should remain in English.

## Backend architecture rules

Dependencies must point inward:

```text
presentation -> application -> domain
infrastructure -------------> domain
```

- `domain` must not depend on Axum, SQLx, PostgreSQL, HTTP, or mobile concerns.
- `application` contains use cases and business rules. It communicates through domain traits such as `UserRepository`.
- `infrastructure` implements domain ports. SQL and database-specific mapping belong here.
- `presentation` translates HTTP requests and responses to application operations.
- `main.rs` is the composition root. It may construct configuration, database pools, repositories, services, and the router.
- Keep transport DTOs separate from persistence models when their responsibilities diverge.
- Do not leak raw SQLx errors, credentials, or internal details to API clients.
- Use structured domain/application errors and map them to stable HTTP status codes in the presentation layer.
- Keep the OpenAPI specification synchronized with actual routes, payloads, status codes, and error bodies.
- Enforce important invariants in PostgreSQL as well as in application code. Email uniqueness, for example, must remain a database constraint even when checked by the service.

## Rust conventions

- Run `cargo fmt` on changed Rust code.
- Keep `cargo clippy` free of newly introduced warnings.
- Avoid `unwrap`, `expect`, and panics in request-handling and startup paths unless an invariant is truly unrecoverable and explained.
- Propagate errors with meaningful types and context.
- Use async operations for I/O and do not block Tokio worker threads.
- Keep public APIs small. Add visibility only where another module or integration test needs it.
- Prefer constructor injection for repositories and services.
- Reuse `InMemoryUserRepository` for fast application tests.
- Use PostgreSQL integration tests for SQL behavior, constraints, and migrations.

Run backend checks from `axum-server/`:

```bash
cargo fmt --check
cargo check
cargo clippy --all-targets --all-features -- -D warnings
cargo test
```

Database-dependent checks require a reachable PostgreSQL instance and a valid `DATABASE_URL`.

## Database rules

- Manage schema changes with versioned SQL migrations. Do not rely on manually creating or editing tables.
- Never modify an already-applied migration to change production behavior; add a new migration instead.
- Migrations should be deterministic and committed with the code that depends on them.
- Use explicit constraints, sensible column sizes, and appropriate indexes.
- Store timestamps with timezone where timestamps are needed.
- Keep database URLs and passwords in environment variables. Commit only safe examples such as `.env.example`.
- Application startup should fail clearly when required database configuration is invalid.
- Tests must not depend on a developer's personal database or destroy persistent development data.

## API rules

- Keep API routes under `/api/v1` until a deliberate version change is required.
- Use JSON consistently for successful bodies and errors, except for responses such as `204 No Content`.
- Validate input at the system boundary and enforce business invariants in the application layer.
- Return stable machine-readable error codes when the error contract is expanded.
- Use `POST` for creation, `GET` for retrieval, `DELETE` for deletion, and prefer `PATCH` for partial updates.
- Preserve backward compatibility or document a migration when changing routes or payloads used by the mobile client.
- Add pagination before treating an unbounded list endpoint as production-ready.

## Kotlin Multiplatform architecture rules

Target dependency flow:

```text
Compose UI -> ViewModel -> UserRepository -> Ktor implementation -> HTTP API
```

- Shared business and presentation logic belongs in `commonMain` when it is truly platform-independent.
- Platform-specific networking configuration or APIs belong in the appropriate platform source set.
- Compose functions should render state and emit events; networking must not occur directly in UI composables.
- ViewModels must not construct concrete API clients when dependency injection can make them testable.
- Keep network DTOs, domain models, repositories, ViewModels, and UI components separate as the application grows.
- Model loading, content, empty, and error states explicitly.
- Do not silently discard server errors. Convert them to useful application-level failures and safe Spanish messages.
- Manage `HttpClient` ownership and closing explicitly.
- Do not assume `10.0.2.2` works on iOS or physical devices. Resolve base URLs per environment/platform.
- Put reusable strings, icons, colors, and dimensions in shared resources or theme definitions rather than duplicating literals.
- Split large UI files when components have independent responsibilities or tests.

Run client checks from `HandleUsers/`:

```bash
./gradlew check
./gradlew :androidApp:assembleDebug
```

iOS builds require macOS and Xcode. If an iOS build was not run, state that explicitly.

## Testing expectations

Every bug fix should include a regression test when practical. Every new behavior should be covered at the lowest useful level.

Backend test priorities:

1. Application tests with `InMemoryUserRepository` for business rules.
2. Router/handler tests for HTTP status codes and JSON contracts.
3. PostgreSQL integration tests for queries, constraints, and migrations.

Client test priorities:

1. Pure model and state calculations in `commonTest`.
2. ViewModel tests using a fake `UserRepository`.
3. Repository tests using a controlled HTTP test server.
4. A small number of Compose UI tests for critical flows.

Tests should cover failure paths, not just successful CRUD operations.

## Configuration and environments

Maintain distinct concepts for:

- Local development.
- Automated tests.
- Staging or demonstration environments.
- Production, if it is introduced later.

Configuration must come from environment variables, build configuration, or platform-specific configuration—not hard-coded production values.

Document platform networking differences:

```text
Android emulator: 10.0.2.2 refers to the development host.
iOS simulator:     localhost can refer to the development host.
Physical device:   use a reachable LAN address or HTTPS endpoint.
```

## Security rules

- Never store plaintext passwords.
- If authentication is added, use established password hashing and token/session libraries rather than custom cryptography.
- Authentication does not replace authorization; verify permissions for every protected operation.
- Do not log passwords, access tokens, database credentials, or sensitive personal information.
- Do not expose internal database or stack errors in HTTP responses.
- Use HTTPS for any non-local environment.
- Treat CORS, rate limiting, request-size limits, and timeouts as deployment concerns to configure deliberately rather than copying permissive defaults.

## Observability and operations

When operational features are introduced:

- Keep structured logging through `tracing`.
- Add a request/correlation ID to request logs and safe error responses.
- Provide separate liveness and readiness endpoints.
- Readiness should verify dependencies needed to serve traffic, including PostgreSQL.
- Shut down gracefully and close the database pool and network clients.
- If the Rust server runs inside a container, bind it to `0.0.0.0`; use `127.0.0.1` only when intentionally limiting it to the host.

## Dependency policy

- Before adding a dependency, determine whether the standard library or an existing dependency already solves the problem.
- Prefer maintained, well-documented libraries with a clear purpose.
- Avoid adding frameworks solely to hide a small amount of straightforward code.
- Keep Rust and Kotlin dependency versions centralized in their existing manifests/catalogs.
- Treat broad dependency upgrades as separate changes from feature work unless the upgrade is required.

## Documentation expectations

The root README should eventually explain:

- Prerequisites.
- Repository structure.
- Environment variables.
- How to start PostgreSQL.
- How to apply migrations.
- How to run the backend.
- How to run Android and iOS.
- How to run checks and tests.
- Where Swagger/OpenAPI is available.
- Known local-networking differences between emulator, simulator, and physical devices.

Any command added to documentation should be run or otherwise verified before it is presented as working.

## Definition of done

A change is complete when, in proportion to its scope:

- The implementation respects the layer boundaries above.
- Relevant tests were added or updated.
- Formatting, static checks, and relevant tests pass.
- API and database changes are documented and migrated.
- No secrets or generated files were added.
- Android and iOS implications were considered for shared Kotlin changes.
- The final report lists what was verified and any checks that could not be run.

## Preferred evolution order

When no more specific priority is provided, improve the application in this order:

1. Monorepo hygiene and root documentation.
2. Reproducible Docker Compose environment.
3. Versioned SQL migrations.
4. Typed backend configuration and per-platform client URLs.
5. Broader backend tests and HTTP contract tests.
6. Client repository abstraction and ViewModel tests.
7. Health checks, request tracing, timeouts, and graceful shutdown.
8. Continuous integration.
9. Server-side pagination and filtering.
10. Authentication and authorization.
11. Containerized backend and a staging deployment.

Do not skip directly to later operational or security features if the earlier foundations remain manual or untested, unless the user explicitly requests it.
