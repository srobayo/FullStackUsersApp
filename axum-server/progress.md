# Tutorial Paso a Paso: Servicio Web de Gestión de Usuarios en Rust con Axum, Utoipa (Swagger) y Arquitectura Limpia

Este documento registra el avance, el diseño arquitectónico y el tutorial paso a paso para construir un microservicio web escalable de usuarios en **Rust** utilizando el framework **Axum**, el runtime asíncrono **Tokio**, documentación interactiva **Swagger (Utoipa)** y principios de **Arquitectura Limpia (Clean Architecture)**.

---

## 📐 Visión General de la Arquitectura Limpia

La Arquitectura Limpia independiza la lógica de negocio central de los detalles tecnológicos externos como marcos web (Axum), bases de datos (PostgreSQL, In-Memory) o herramientas de documentación.

```
       +-------------------------------------------------------+
       |                  PRESENTACIÓN / WEB                   |
       |  (Axum Handlers, Rutas, Utoipa OpenAPI, Swagger UI)   |
       +---------------------------+---------------------------+
                                   |
                                   v
       +-------------------------------------------------------+
       |                 APLICACIÓN / USE CASES                |
       |            (UserService, Lógica de Negocio)           |
       +---------------------------+---------------------------+
                                   |
                                   v
       +-------------------------------------------------------+
       |                        DOMINIO                        |
       |    (User Entity, UserError, UserRepository Trait)     |
       +-------------------------------------------------------+
                                   ^
                                   |
       +---------------------------+---------------------------+
       |                    INFRAESTRUCTURA                    |
       |  (InMemoryUserRepository, PostgresUserRepository)    |
       +-------------------------------------------------------+
```

### Principios Fundamentales:
1. **Regla de Dependencia**: Las capas internas (Dominio) no conocen a las capas externas (Axum, SQLx). Las dependencias siempre apuntan hacia adentro.
2. **Abstracción de Persistencia (Puertos y Adaptadores)**: La aplicación define el contrato de datos mediante el trait `UserRepository`. Cambiar de persistencia en memoria a PostgreSQL no requiere alterar la lógica de negocio ni los controladores HTTP.
3. **Documentación como Código**: Usamos `utoipa` para derivar automáticamente la especificación OpenAPI v3 a partir de los datos y controladores.

---

## 📁 Estructura del Proyecto

```text
axum-server/
├── Cargo.toml
├── progress.md
├── tests/
│   └── user_service_test.rs         # Pruebas unitarias de casos de uso sin capa HTTP
└── src/
    ├── lib.rs                       # Exportación modular de la librería
    ├── main.rs                      # Composition Root (Ensamblado del servidor Tokio/Axum)
    ├── domain/                      # Capa de Dominio (Entidades y Abstracciones)
    │   ├── mod.rs
    │   ├── errors.rs                # Errores del Dominio (UserError)
    │   ├── models/
    │   │   ├── mod.rs
    │   │   └── user.rs              # Entidad User y DTOs (CreateUserPayload, etc.)
    │   └── repositories.rs          # Trait UserRepository (Puerto)
    ├── application/                 # Capa de Aplicación (Casos de Uso)
    │   ├── mod.rs
    │   └── user_service.rs          # Servicio de Negocio
    ├── infrastructure/              # Capa de Infraestructura (Adaptadores)
    │   ├── mod.rs
    │   └── persistence/
    │       ├── mod.rs
    │       └── in_memory_user_repository.rs # Implementación en memoria con RwLock
    └── presentation/                # Capa de Presentación (HTTP / Swagger)
        ├── mod.rs
        ├── handlers/
        │   ├── mod.rs
        │   └── user_handler.rs      # Controladores Axum + Anotaciones Utoipa
        ├── openapi.rs               # Estructura ApiDoc de Utoipa
        └── routes.rs                # Definición del Router Axum y Swagger UI
```

---

## 🛠️ Paso 1: Configuración de Dependencias (`Cargo.toml`)

En el archivo `Cargo.toml` agregamos las librerías necesarias:

```toml
[package]
name = "axum-server"
version = "0.1.0"
edition = "2024"

[dependencies]
axum = "0.7"
tokio = { version = "1", features = ["full"] }
serde = { version = "1.0", features = ["derive"] }
serde_json = "1.0"
uuid = { version = "1.10", features = ["v4", "serde"] }
thiserror = "1.0"
async-trait = "0.1"
tracing = "0.1"
tracing-subscriber = { version = "0.3", features = ["env-filter"] }
utoipa = { version = "4.2", features = ["axum_extras", "uuid"] }
utoipa-swagger-ui = { version = "7.1", features = ["axum"] }
```

---

## 🟢 Paso 2: Capa de Dominio (`src/domain/`)

### 2.1 Modelos y Entidades (`src/domain/models/user.rs`)
Definimos la entidad `User` y los payloads de creación/actualización con esquemas de `utoipa`.

```rust
use serde::{Deserialize, Serialize};
use utoipa::ToSchema;
use uuid::Uuid;

#[derive(Debug, Clone, Serialize, Deserialize, ToSchema)]
pub struct User {
    #[schema(example = "550e8400-e29b-41d4-a716-446655440000")]
    pub id: Uuid,
    #[schema(example = "Juan Pérez")]
    pub name: String,
    #[schema(example = "juan.perez@example.com")]
    pub email: String,
    #[schema(example = true)]
    pub active: bool,
}

#[derive(Debug, Clone, Serialize, Deserialize, ToSchema)]
pub struct CreateUserPayload {
    #[schema(example = "Juan Pérez")]
    pub name: String,
    #[schema(example = "juan.perez@example.com")]
    pub email: String,
}

#[derive(Debug, Clone, Serialize, Deserialize, ToSchema)]
pub struct UpdateUserPayload {
    #[schema(example = "Juan Pérez Actualizado")]
    pub name: Option<String>,
    #[schema(example = "juan.actualizado@example.com")]
    pub email: Option<String>,
    #[schema(example = false)]
    pub active: Option<bool>,
}
```

### 2.2 Errores del Dominio (`src/domain/errors.rs`)
Gestión centralizada de errores con `thiserror`.

```rust
use thiserror::Error;

#[derive(Debug, Error)]
pub enum UserError {
    #[error("Usuario con ID '{0}' no encontrado")]
    NotFound(uuid::Uuid),

    #[error("El correo electrónico '{0}' ya se encuentra registrado")]
    EmailAlreadyExists(String),

    #[error("Error de validación: {0}")]
    ValidationError(String),

    #[error("Error interno en la capa de persistencia/infraestructura: {0}")]
    RepositoryError(String),
}
```

### 2.3 Puerto de Persistencia (`src/domain/repositories.rs`)
Trait asíncrono para abstraer el almacenamiento de datos.

```rust
use async_trait::async_trait;
use uuid::Uuid;
use super::errors::UserError;
use super::models::User;

#[async_trait]
pub trait UserRepository: Send + Sync {
    async fn create(&self, user: User) -> Result<User, UserError>;
    async fn find_by_id(&self, id: Uuid) -> Result<Option<User>, UserError>;
    async fn find_by_email(&self, email: &str) -> Result<Option<User>, UserError>;
    async fn list_all(&self) -> Result<Vec<User>, UserError>;
    async fn update(&self, user: User) -> Result<User, UserError>;
    async fn delete(&self, id: Uuid) -> Result<(), UserError>;
}
```

---

## ⚙️ Paso 3: Capa de Aplicación (`src/application/`)

El `UserService` implementa la lógica de negocio e interpola los casos de uso.

```rust
use std::sync::Arc;
use uuid::Uuid;
use crate::domain::{CreateUserPayload, UpdateUserPayload, User, UserError, UserRepository};

pub struct UserService {
    repo: Arc<dyn UserRepository>,
}

impl UserService {
    pub fn new(repo: Arc<dyn UserRepository>) -> Self {
        Self { repo }
    }

    pub async fn create_user(&self, payload: CreateUserPayload) -> Result<User, UserError> {
        if payload.name.trim().is_empty() {
            return Err(UserError::ValidationError("El nombre no puede estar vacío".to_string()));
        }
        if !payload.email.contains('@') {
            return Err(UserError::ValidationError("Correo electrónico inválido".to_string()));
        }
        if let Some(_) = self.repo.find_by_email(&payload.email).await? {
            return Err(UserError::EmailAlreadyExists(payload.email));
        }

        let new_user = User {
            id: Uuid::new_v4(),
            name: payload.name,
            email: payload.email,
            active: true,
        };

        self.repo.create(new_user).await
    }

    pub async fn get_user_by_id(&self, id: Uuid) -> Result<User, UserError> {
        self.repo.find_by_id(id).await?.ok_or(UserError::NotFound(id))
    }

    pub async fn list_users(&self) -> Result<Vec<User>, UserError> {
        self.repo.list_all().await
    }

    pub async fn update_user(&self, id: Uuid, payload: UpdateUserPayload) -> Result<User, UserError> {
        let mut user = self.get_user_by_id(id).await?;

        if let Some(name) = payload.name {
            if name.trim().is_empty() {
                return Err(UserError::ValidationError("El nombre no puede estar vacío".to_string()));
            }
            user.name = name;
        }

        if let Some(email) = payload.email {
            if !email.contains('@') {
                return Err(UserError::ValidationError("Email inválido".to_string()));
            }
            if email != user.email {
                if let Some(_) = self.repo.find_by_email(&email).await? {
                    return Err(UserError::EmailAlreadyExists(email));
                }
            }
            user.email = email;
        }

        if let Some(active) = payload.active {
            user.active = active;
        }

        self.repo.update(user).await
    }

    pub async fn delete_user(&self, id: Uuid) -> Result<(), UserError> {
        let _ = self.get_user_by_id(id).await?;
        self.repo.delete(id).await
    }
}
```

---

## 💾 Paso 4: Capa de Infraestructura (`src/infrastructure/`)

### 4.1 Persistencia en Memoria (`src/infrastructure/persistence/in_memory_user_repository.rs`)
Implementación concurrente utilizando `Arc<RwLock<HashMap<Uuid, User>>>`.

### 4.2 🚀 Guía de Escalabilidad para PostgreSQL (Futura Integración)
Para integrar PostgreSQL usando **SQLx**:
1. Agregar la dependencia en `Cargo.toml`: `sqlx = { version = "0.8", features = ["runtime-tokio-native-tls", "postgres", "uuid"] }`.
2. Crear un struct `PostgresUserRepository { pool: PgPool }`.
3. Implementar `#[async_trait] impl UserRepository for PostgresUserRepository`.
4. En `src/main.rs`, simplemente cambiar:
   ```rust
   // De:
   // let user_repo = Arc::new(InMemoryUserRepository::new());
   // A:
   let pool = PgPoolOptions::new().connect(&database_url).await?;
   let user_repo = Arc::new(PostgresUserRepository::new(pool));
   ```
¡Ninguna otra línea del sistema necesitará cambios!

---

## 🌐 Paso 5: Capa de Presentación / HTTP (`src/presentation/`)

### 5.1 Controladores Axum y Annotaciones Utoipa (`src/presentation/handlers/user_handler.rs`)
Mapeamos errores de dominio a respuestas HTTP con códigos de estado (400, 404, 409, 500) y JSON descriptivos.

### 5.2 Documentación Swagger (`src/presentation/openapi.rs` y `src/presentation/routes.rs`)
Configuramos la ruta `/swagger-ui` que sirve la interfaz interactiva.

---

## 🚀 Paso 6: Punto de Entrada (`src/main.rs`)

Inicializa los logs con `tracing_subscriber`, realiza la inyección de dependencias y levanta el servidor HTTP con `tokio`.

---

## 🧪 Pruebas Automáticas (`tests/user_service_test.rs`)

Ejecuta las pruebas unitarias desvinculadas de la red:
```bash
cargo test
```
Resultados de ejecución:
- `test_create_user_success` ... OK
- `test_create_user_invalid_email` ... OK
- `test_prevent_duplicate_email` ... OK

---

## 📖 Instrucciones de Uso y Prueba Manual

### 1. Iniciar el Servidor
```bash
cargo run
```
Salida esperada en consola:
```text
INFO axum_server: Iniciando Servidor Axum con Arquitectura Limpia...
INFO axum_server: 🚀 Servidor ejecutándose exitosamente en http://127.0.0.1:3000
INFO axum_server: 📚 Documentación Swagger UI disponible en http://127.0.0.1:3000/swagger-ui/
```

### 2. Probar mediante Swagger UI
Abre tu navegador e ingresa a: **`http://127.0.0.1:3000/swagger-ui/`**
Podrás probar interactiva y visualmente los endpoints:
- `POST /api/v1/users`
- `GET /api/v1/users`
- `GET /api/v1/users/{id}`
- `PUT /api/v1/users/{id}`
- `DELETE /api/v1/users/{id}`

### 3. Ejemplos de Comandos `cURL`

#### Crear un Usuario
```bash
curl -X POST http://127.0.0.1:3000/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Ana Martínez", "email": "ana.martinez@example.com"}'
```

#### Listar Usuarios
```bash
curl -X GET http://127.0.0.1:3000/api/v1/users
```

#### Obtener Usuario por ID
```bash
curl -X GET http://127.0.0.1:3000/api/v1/users/<ID_UUID>
```

#### Actualizar Usuario
```bash
curl -X PUT http://127.0.0.1:3000/api/v1/users/<ID_UUID> \
  -H "Content-Type: application/json" \
  -d '{"name": "Ana M. Actualizada", "active": false}'
```

#### Eliminar Usuario
```bash
curl -X DELETE http://127.0.0.1:3000/api/v1/users/<ID_UUID>
```

---

## 🐘 Paso 7: Integración con PostgreSQL en Docker

### 7.1 Definición de la Tabla `users` en PostgreSQL
Se eliminó la tabla previa y se creó una nueva tabla completamente compatible con el modelo de dominio en Rust:

```sql
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
```

### 7.2 Adaptador de Infraestructura PostgreSQL (`src/infrastructure/persistence/postgres_user_repository.rs`)
Implementamos el trait `UserRepository` utilizando `sqlx::PgPool`:

```rust
#[derive(Clone)]
pub struct PostgresUserRepository {
    pool: PgPool,
}

impl PostgresUserRepository {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
}

#[async_trait]
impl UserRepository for PostgresUserRepository {
    async fn create(&self, user: User) -> Result<User, UserError> { ... }
    async fn find_by_id(&self, id: Uuid) -> Result<Option<User>, UserError> { ... }
    async fn find_by_email(&self, email: &str) -> Result<Option<User>, UserError> { ... }
    async fn list_all(&self) -> Result<Vec<User>, UserError> { ... }
    async fn update(&self, user: User) -> Result<User, UserError> { ... }
    async fn delete(&self, id: Uuid) -> Result<(), UserError> { ... }
}
```

### 7.3 Actualización de `src/main.rs` (Composition Root)
Conectamos la aplicación a la base de datos PostgreSQL utilizando la variable de entorno `DATABASE_URL` o el valor por defecto:

```rust
let db_url = env::var("DATABASE_URL")
    .unwrap_or_else(|_| "postgres://user:password@127.0.0.1:5433/simple_api".to_string());

let pool = PgPoolOptions::new()
    .max_connections(5)
    .connect(&db_url)
    .await?;

let user_repo = Arc::new(PostgresUserRepository::new(pool));
let user_service = Arc::new(UserService::new(user_repo));
```

---

## ✅ Registro de Estado de Avance

- [x] Inicialización del proyecto Cargo `axum-server`.
- [x] Definición de dependencias (`axum`, `tokio`, `utoipa`, `utoipa-swagger-ui`, `serde`, `uuid`, `sqlx`).
- [x] Capa de Dominio: Entidades, DTOs con esquemas Utoipa, Errores de Dominio y Trait `UserRepository`.
- [x] Capa de Aplicación: Servicio `UserService` con validaciones de negocio.
- [x] Capa de Infraestructura: Adaptador en memoria (`InMemoryUserRepository`) y Adaptador de PostgreSQL (`PostgresUserRepository` con SQLx).
- [x] Creación y migración de la tabla `users` en la base de datos PostgreSQL (`simple_api`) en Docker.
- [x] Capa de Presentación: Handlers de Axum, mapeo de respuestas de error JSON y UI Swagger.
- [x] Punto de entrada `main.rs` configurado con conexión a PostgreSQL en Docker y logs Tracing.
- [x] Suite de Pruebas Unitarias automatizadas en `tests/user_service_test.rs`.

