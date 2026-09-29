# FullStackUsersApp

Educational monorepo for evolving a small user-management application toward production-quality practices while learning Rust, PostgreSQL, Kotlin, and Compose Multiplatform.

## Projects

```text
FullStackUsersApp/
├── axum-server/   # Rust backend with Axum, SQLx, PostgreSQL, and OpenAPI
└── HandleUsers/   # Kotlin and Compose Multiplatform client for Android and iOS
```

The backend exposes a JSON CRUD API under `/api/v1/users`. The mobile client consumes that API to list, create, update, activate, deactivate, and delete users.

## Prerequisites

- Rust toolchain and Cargo
- Docker Desktop or another Docker Engine with Compose
- JDK compatible with the included Gradle project
- Android Studio and the Android SDK for Android development
- macOS and Xcode for iOS development

## Local configuration

Create a local environment file from the committed example:

```bash
cp .env.example .env
```

The `.env` file is ignored by Git. Its default values are intended only for local development and must not be reused in a public or production environment.

Backend settings:

| Variable | Required | Default | Purpose |
| --- | --- | --- | --- |
| `DATABASE_URL` | Yes | None | PostgreSQL connection string |
| `SERVER_HOST` | No | `127.0.0.1` | HTTP bind address |
| `SERVER_PORT` | No | `3000` | HTTP port |
| `DATABASE_MAX_CONNECTIONS` | No | `5` | Maximum PostgreSQL pool connections |
| `RUST_LOG` | No | Application default | Logging filter |

The backend validates these values before connecting to PostgreSQL. It exits with a clear error when `DATABASE_URL` is missing or when a numeric/network value is invalid. Connection credentials are not printed in application logs.

`compose.yaml` defines PostgreSQL 15, a persistent named volume, and a readiness health check. Validate the resolved configuration with:

```bash
docker compose --env-file .env.example config
```

The backend applies the versioned SQL migrations from `axum-server/migrations/` after connecting to PostgreSQL and before accepting HTTP requests. The initial migration uses `IF NOT EXISTS` only to adopt the verified legacy `users` schema during this transition; future migrations should describe explicit schema changes. The normal local database lifecycle is:

```bash
docker compose up -d postgres
docker compose ps
docker compose down
```

`docker compose down` preserves the named database volume. Avoid `docker compose down -v` unless deleting all local database data is intentional.

The database transition was completed on 2026-09-29. PostgreSQL managed by this monorepo now uses port `5433` and the `fullstack-users-app_postgres_data` volume. The previous `db` and `simple_axum` containers are stopped but retained as a temporary rollback option.

Check the active database with:

```bash
docker compose ps
```

## Run the backend

From `axum-server/`:

```bash
set -a
source ../.env
set +a
cargo run
```

When running, the current development server is available at:

- API: `http://127.0.0.1:3000/api/v1/users`
- Swagger UI: `http://127.0.0.1:3000/swagger-ui/`

If the OpenAPI JSON responds but Swagger UI returns `404` after moving or renaming the repository, remove the stale Rust build cache and rebuild:

```bash
cd axum-server
cargo clean
cargo run
```

In debug builds, Swagger UI assets can retain an absolute path from the previous checkout location.

## Run the Android client

Open `HandleUsers/` in Android Studio or build it from that directory:

```bash
./gradlew :androidApp:assembleDebug
```

The Android emulator reaches a backend running on the development host through `http://10.0.2.2:3000`.

## Run the iOS client

Open `HandleUsers/iosApp/` in Xcode and run the `iosApp` scheme. Local backend addressing differs between the iOS simulator and physical devices and must be configured accordingly.

## Checks

Backend checks, from `axum-server/`:

```bash
cargo fmt --check
cargo check
cargo clippy --all-targets --all-features -- -D warnings
cargo test
```

Client checks, from `HandleUsers/`:

```bash
./gradlew check
./gradlew :androidApp:assembleDebug
```

## Current development roadmap

1. Move backend and client configuration out of source-code defaults.
2. Expand backend, HTTP contract, ViewModel, and UI tests.
3. Add health checks, request tracing, timeouts, and graceful shutdown.
4. Add continuous integration.

Repository-wide development conventions are documented in [AGENTS.md](./AGENTS.md).
