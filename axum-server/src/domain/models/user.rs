use serde::{Deserialize, Serialize};
use utoipa::ToSchema;
use uuid::Uuid;

/// Entidad Dominio que representa a un Usuario en el sistema.
#[derive(Debug, Clone, Serialize, Deserialize, ToSchema)]
pub struct User {
    /// Identificador único del usuario (UUID v4)
    #[schema(example = "550e8400-e29b-41d4-a716-446655440000")]
    pub id: Uuid,
    /// Nombre completo del usuario
    #[schema(example = "Juan Pérez")]
    pub name: String,
    /// Correo electrónico único del usuario
    #[schema(example = "juan.perez@example.com")]
    pub email: String,
    /// Estado de actividad de la cuenta del usuario
    #[schema(example = true)]
    pub active: bool,
}

/// DTO / Payload para la creación de un nuevo usuario.
#[derive(Debug, Clone, Serialize, Deserialize, ToSchema)]
pub struct CreateUserPayload {
    /// Nombre completo del usuario a crear
    #[schema(example = "Juan Pérez")]
    pub name: String,
    /// Correo electrónico del usuario
    #[schema(example = "juan.perez@example.com")]
    pub email: String,
}

/// DTO / Payload para actualizar un usuario existente.
#[derive(Debug, Clone, Serialize, Deserialize, ToSchema)]
pub struct UpdateUserPayload {
    /// Nombre opcional a actualizar
    #[schema(example = "Juan Pérez Actualizado")]
    pub name: Option<String>,
    /// Correo electrónico opcional a actualizar
    #[schema(example = "juan.actualizado@example.com")]
    pub email: Option<String>,
    /// Estado activo del usuario
    #[schema(example = false)]
    pub active: Option<bool>,
}
