use sqlx::postgres::PgPoolOptions;
use std::env;
use std::sync::Arc;
use tokio::net::TcpListener;
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

mod application;
mod domain;
mod infrastructure;
mod presentation;

use application::UserService;
use infrastructure::PostgresUserRepository;
use presentation::{AppState, create_router};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    // 1. Inicializar el sistema de logs (Tracing)
    tracing_subscriber::registry()
        .with(
            tracing_subscriber::EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| "axum_server=debug,tower_http=debug,sqlx=info".into()),
        )
        .with(tracing_subscriber::fmt::layer())
        .init();

    tracing::info!("Iniciando Servidor Axum con Arquitectura Limpia y PostgreSQL...");

    // 2. Conexión a la Base de Datos PostgreSQL en Docker
    let db_url = env::var("DATABASE_URL")
        .unwrap_or_else(|_| "postgres://user:password@127.0.0.1:5433/simple_api".to_string());

    tracing::info!("Conectando a PostgreSQL en {}", db_url);
    let pool = PgPoolOptions::new()
        .max_connections(5)
        .connect(&db_url)
        .await?;

    tracing::info!("Conexión a PostgreSQL establecida con éxito.");

    // Aplicar las migraciones versionadas antes de aceptar peticiones.
    sqlx::migrate!("./migrations").run(&pool).await?;
    tracing::info!("Migraciones de base de datos aplicadas correctamente.");

    // 3. Inyección de Dependencias (Composition Root)
    // Instanciar el adaptador de infraestructura de PostgreSQL
    let user_repo = Arc::new(PostgresUserRepository::new(pool));

    // Instanciar el servicio de aplicación con el repositorio inyectado
    let user_service = Arc::new(UserService::new(user_repo));

    // Crear el estado compartido de la aplicación web
    let app_state = AppState { user_service };

    // 4. Crear el Router de Axum con endpoints y Swagger UI
    let app = create_router(app_state);

    // 5. Iniciar el servidor HTTP Tokio
    let addr = "127.0.0.1:3000";
    let listener = TcpListener::bind(addr).await?;

    tracing::info!("🚀 Servidor ejecutándose exitosamente en http://{}", addr);
    tracing::info!(
        "📚 Documentación Swagger UI disponible en http://{}/swagger-ui/",
        addr
    );

    axum::serve(listener, app).await?;

    Ok(())
}
