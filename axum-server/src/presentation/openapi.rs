use utoipa::OpenApi;

use crate::domain::{CreateUserPayload, UpdateUserPayload, User};
use crate::presentation::handlers::user_handler::{
    __path_create_user_handler, __path_delete_user_handler, __path_get_user_handler,
    __path_list_users_handler, __path_update_user_handler, ErrorResponse,
};

/// Documento OpenAPI para la API del servicio de usuarios.
#[derive(OpenApi)]
#[openapi(
    paths(
        create_user_handler,
        get_user_handler,
        list_users_handler,
        update_user_handler,
        delete_user_handler
    ),
    components(
        schemas(User, CreateUserPayload, UpdateUserPayload, ErrorResponse)
    ),
    tags(
        (name = "Usuarios", description = "Endpoints para la gestión de usuarios usando Arquitectura Limpia")
    ),
    info(
        title = "Axum User Management API",
        version = "1.0.0",
        description = "Tutorial de Microservicio Web en Rust con Axum, Utoipa (Swagger) y Arquitectura Limpia."
    )
)]
pub struct ApiDoc;
