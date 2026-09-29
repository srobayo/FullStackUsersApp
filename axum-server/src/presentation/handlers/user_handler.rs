use axum::{
    Json,
    extract::{Path, State},
    http::StatusCode,
    response::{IntoResponse, Response},
};
use serde::Serialize;
use std::sync::Arc;
use utoipa::ToSchema;
use uuid::Uuid;

use crate::application::UserService;
use crate::domain::{CreateUserPayload, UpdateUserPayload, User, UserError};

/// Definición de Estado de la Aplicación (AppState) para Axum.
#[derive(Clone)]
pub struct AppState {
    pub user_service: Arc<UserService>,
}

/// DTO de respuesta para mensajes de error formateados en JSON.
#[derive(Serialize, ToSchema)]
pub struct ErrorResponse {
    pub error: String,
}

// Convertir los errores del Dominio a Respuestas HTTP de Axum
impl IntoResponse for UserError {
    fn into_response(self) -> Response {
        let (status, message) = match self {
            UserError::NotFound(id) => (
                StatusCode::NOT_FOUND,
                format!("Usuario con id '{}' no fue encontrado", id),
            ),
            UserError::EmailAlreadyExists(email) => (
                StatusCode::CONFLICT,
                format!("El correo '{}' ya está en uso", email),
            ),
            UserError::ValidationError(msg) => (StatusCode::BAD_REQUEST, msg),
            UserError::RepositoryError(msg) => (
                StatusCode::INTERNAL_SERVER_ERROR,
                format!("Error interno: {}", msg),
            ),
        };

        let body = Json(ErrorResponse { error: message });
        (status, body).into_response()
    }
}

/// Registrar un nuevo usuario
#[utoipa::path(
    post,
    path = "/api/v1/users",
    request_body = CreateUserPayload,
    responses(
        (status = 201, description = "Usuario creado exitosamente", body = User),
        (status = 400, description = "Error de validación en la solicitud", body = ErrorResponse),
        (status = 409, description = "El correo electrónico ya existe", body = ErrorResponse),
        (status = 500, description = "Error interno del servidor", body = ErrorResponse)
    ),
    tag = "Usuarios"
)]
pub async fn create_user_handler(
    State(state): State<AppState>,
    Json(payload): Json<CreateUserPayload>,
) -> Result<(StatusCode, Json<User>), UserError> {
    let user = state.user_service.create_user(payload).await?;
    Ok((StatusCode::CREATED, Json(user)))
}

/// Obtener un usuario por su ID (UUID)
#[utoipa::path(
    get,
    path = "/api/v1/users/{id}",
    params(
        ("id" = Uuid, Path, description = "UUID del usuario a obtener")
    ),
    responses(
        (status = 200, description = "Usuario encontrado", body = User),
        (status = 404, description = "Usuario no encontrado", body = ErrorResponse),
        (status = 500, description = "Error interno del servidor", body = ErrorResponse)
    ),
    tag = "Usuarios"
)]
pub async fn get_user_handler(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
) -> Result<Json<User>, UserError> {
    let user = state.user_service.get_user_by_id(id).await?;
    Ok(Json(user))
}

/// Listar todos los usuarios
#[utoipa::path(
    get,
    path = "/api/v1/users",
    responses(
        (status = 200, description = "Lista completa de usuarios", body = [User]),
        (status = 500, description = "Error interno del servidor", body = ErrorResponse)
    ),
    tag = "Usuarios"
)]
pub async fn list_users_handler(
    State(state): State<AppState>,
) -> Result<Json<Vec<User>>, UserError> {
    let users = state.user_service.list_users().await?;
    Ok(Json(users))
}

/// Actualizar información de un usuario
#[utoipa::path(
    put,
    path = "/api/v1/users/{id}",
    params(
        ("id" = Uuid, Path, description = "UUID del usuario a actualizar")
    ),
    request_body = UpdateUserPayload,
    responses(
        (status = 200, description = "Usuario actualizado exitosamente", body = User),
        (status = 400, description = "Error de validación", body = ErrorResponse),
        (status = 404, description = "Usuario no encontrado", body = ErrorResponse),
        (status = 409, description = "El nuevo correo ya existe", body = ErrorResponse)
    ),
    tag = "Usuarios"
)]
pub async fn update_user_handler(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
    Json(payload): Json<UpdateUserPayload>,
) -> Result<Json<User>, UserError> {
    let user = state.user_service.update_user(id, payload).await?;
    Ok(Json(user))
}

/// Eliminar un usuario
#[utoipa::path(
    delete,
    path = "/api/v1/users/{id}",
    params(
        ("id" = Uuid, Path, description = "UUID del usuario a eliminar")
    ),
    responses(
        (status = 204, description = "Usuario eliminado exitosamente"),
        (status = 404, description = "Usuario no encontrado", body = ErrorResponse)
    ),
    tag = "Usuarios"
)]
pub async fn delete_user_handler(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
) -> Result<StatusCode, UserError> {
    state.user_service.delete_user(id).await?;
    Ok(StatusCode::NO_CONTENT)
}
