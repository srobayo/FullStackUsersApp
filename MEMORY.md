# Project Memory

Registro vivo de decisiones, estado conocido y asuntos pendientes de `FullStackUsersApp`.

Este archivo debe actualizarse cuando se tome una decisión arquitectónica importante, se detecte deuda técnica relevante, cambie el procedimiento de desarrollo o quede una tarea que convenga recordar. No debe contener secretos, credenciales ni información sensible.

## Contexto del proyecto

- Aplicación educativa para aprender backend con Rust, Axum y SQLx; PostgreSQL; Kotlin; y Compose Multiplatform.
- Cliente móvil compartido para Android e iOS.
- PostgreSQL se ejecuta localmente en un contenedor Docker.
- El objetivo es evolucionar gradualmente desde un CRUD de aprendizaje hacia prácticas más profesionales, sin introducir complejidad injustificada.

## Decisiones vigentes

- El proyecto se administra como monorepo.
- Estructura principal:

  ```text
  FullStackUsersApp/
  ├── axum-server/
  └── HandleUsers/
  ```

- El repositorio remoto principal es:
  `https://github.com/srobayo/FullStackUsersApp.git`
- La rama principal es `main`.
- Los textos visibles para el usuario permanecen en español y los identificadores de código en inglés.
- Los cambios deben realizarse en etapas pequeñas y verificables, manteniendo la aplicación funcional.

## Estado confirmado

### 2026-09-29 — Publicación inicial en GitHub

- El commit local y `origin/main` coinciden en:
  `61adbc38915aba4543dcb74a8ed9d8e5fd1be543`.
- El commit publicado se llama `First commit`.
- Están publicados:
  - `AGENTS.md`
  - `README.md`
  - `axum-server/`
  - `HandleUsers/`
- El repositorio remoto quedó accesible mediante HTTPS.

### Verificación previa a la publicación

- `cargo check` terminó correctamente.
- `cargo test` terminó correctamente con 3 pruebas superadas.
- `./gradlew clean check :androidApp:assembleDebug` terminó correctamente.
- El APK Android se generó correctamente.
- No se verificó una compilación completa de la aplicación iOS mediante Xcode.

### 2026-09-29 — Higiene del repositorio restaurada

- Se restauró el `.gitignore` raíz para excluir secretos locales, archivos de IDE y artefactos de Rust, Gradle, Kotlin, Android y Xcode.
- Se restauró `.gitattributes` para normalizar los finales de línea entre macOS, Linux y Windows.
- `.DS_Store` quedó ignorado y no debe formar parte del repositorio.

### 2026-09-29 — Calidad base del backend Rust

- Todo el backend fue normalizado con `cargo fmt`.
- Se corrigieron las cinco advertencias conocidas de Clippy sin cambiar el comportamiento funcional:
  - Las búsquedas opcionales usan `is_some()`.
  - La validación de correo duplicado durante una actualización se simplificó.
  - `InMemoryUserRepository` implementa `Default`.
  - Las actualizaciones en memoria usan `get_mut` y evitan una doble búsqueda en el `HashMap`.
- `cargo fmt --check`, `cargo check` y Clippy con `-D warnings` terminaron correctamente.
- `cargo test` terminó correctamente con 3 pruebas superadas.

## Pendientes conocidos

### Higiene del repositorio

- El historial anterior del backend no aparece en el repositorio publicado; actualmente GitHub contiene un único commit inicial.

### Próximas mejoras recomendadas

1. Añadir un entorno PostgreSQL reproducible con `compose.yaml`.
2. Añadir `.env.example` sin credenciales reales.
3. Introducir migraciones SQL versionadas.
4. Centralizar la configuración del backend.
5. Configurar URLs del servidor por plataforma y entorno en el cliente.
6. Ampliar pruebas del backend y añadir pruebas del ViewModel.
7. Añadir health checks, trazabilidad de peticiones y apagado ordenado.
8. Añadir integración continua.

## Reglas para mantener este archivo

- Registrar hechos confirmados, no suposiciones.
- Añadir fecha a las entradas de estado importantes.
- Mover un asunto fuera de “Pendientes conocidos” cuando se complete y dejar una nota breve de la resolución.
- Evitar copiar logs extensos; conservar solo el resultado y la implicación.
- No guardar contraseñas, tokens, claves SSH, URLs con credenciales ni contenido de `.env`.
- Mantener las instrucciones operativas completas en `README.md` o `AGENTS.md`; este archivo funciona como memoria resumida del proyecto.
