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
- PostgreSQL running locally
- JDK compatible with the included Gradle project
- Android Studio and the Android SDK for Android development
- macOS and Xcode for iOS development

The PostgreSQL database is currently expected at the connection string configured by `DATABASE_URL`. If that variable is absent, the backend uses its existing local development default.

## Run the backend

From `axum-server/`:

```bash
export DATABASE_URL="postgres://user:password@127.0.0.1:5433/simple_api"
cargo run
```

When running, the current development server is available at:

- API: `http://127.0.0.1:3000/api/v1/users`
- Swagger UI: `http://127.0.0.1:3000/swagger-ui/`

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

1. Consolidate and document the monorepo.
2. Add a reproducible Docker Compose environment for PostgreSQL.
3. Introduce versioned SQL migrations.
4. Move backend and client configuration out of source-code defaults.
5. Expand backend, HTTP contract, ViewModel, and UI tests.
6. Add health checks, request tracing, timeouts, and graceful shutdown.
7. Add continuous integration.

Repository-wide development conventions are documented in [AGENTS.md](./AGENTS.md).
