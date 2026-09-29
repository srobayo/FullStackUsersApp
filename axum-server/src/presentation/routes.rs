use axum::{
    routing::{get, post},
    Router,
};
use utoipa::OpenApi;
use utoipa_swagger_ui::SwaggerUi;

use super::handlers::{
    create_user_handler, delete_user_handler, get_user_handler, list_users_handler,
    update_user_handler, AppState,
};
use super::openapi::ApiDoc;

/// Construye el Router principal de Axum registrando todas las rutas de usuarios y la interfaz Swagger UI.
pub fn create_router(state: AppState) -> Router {
    let api_routes = Router::new()
        .route("/users", post(create_user_handler).get(list_users_handler))
        .route(
            "/users/:id",
            get(get_user_handler)
                .put(update_user_handler)
                .delete(delete_user_handler),
        );

    Router::new()
        .nest("/api/v1", api_routes)
        .merge(SwaggerUi::new("/swagger-ui").url("/api-docs/openapi.json", ApiDoc::openapi()))
        .with_state(state)
}
