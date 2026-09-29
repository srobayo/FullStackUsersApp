use thiserror::Error;

/// Errores del Dominio relacionados con la gestión de usuarios.
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
