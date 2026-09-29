use sqlx::postgres::PgPoolOptions;
use std::sync::Arc;
use tokio::net::TcpListener;
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

use axum_server::application::UserService;
use axum_server::config::Settings;
use axum_server::infrastructure::PostgresUserRepository;
use axum_server::presentation::{AppState, create_router};

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

    // 2. Cargar y validar la configuración del proceso.
    let settings = Settings::from_env()?;
    tracing::info!(
        server_address = %settings.server_address,
        database_max_connections = settings.database_max_connections,
        "Configuración cargada correctamente."
    );

    // 3. Conexión a la Base de Datos PostgreSQL en Docker.
    tracing::info!("Conectando a PostgreSQL.");
    let pool = PgPoolOptions::new()
        .max_connections(settings.database_max_connections)
        .connect(&settings.database_url)
        .await?;

    tracing::info!("Conexión a PostgreSQL establecida con éxito.");

    // Aplicar las migraciones versionadas antes de aceptar peticiones.
    sqlx::migrate!("./migrations").run(&pool).await?;
    tracing::info!("Migraciones de base de datos aplicadas correctamente.");

    // 4. Inyección de Dependencias (Composition Root)
    // Instanciar el adaptador de infraestructura de PostgreSQL
    let user_repo = Arc::new(PostgresUserRepository::new(pool));

    // Instanciar el servicio de aplicación con el repositorio inyectado
    let user_service = Arc::new(UserService::new(user_repo));

    // Crear el estado compartido de la aplicación web
    let app_state = AppState { user_service };

    // 5. Crear el Router de Axum con endpoints y Swagger UI
    let app = create_router(app_state);

    // 6. Iniciar el servidor HTTP Tokio
    let listener = TcpListener::bind(settings.server_address).await?;

    tracing::info!(
        "🚀 Servidor ejecutándose exitosamente en http://{}",
        settings.server_address
    );
    tracing::info!(
        "📚 Documentación Swagger UI disponible en http://{}/swagger-ui/",
        settings.server_address
    );

    axum::serve(listener, app).await?;

    Ok(())
}
