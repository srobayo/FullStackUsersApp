use async_trait::async_trait;
use uuid::Uuid;
use super::errors::UserError;
use super::models::User;

/// Puerto (Trait) que define los métodos de persistencia para la entidad User.
/// Siguiendo Arquitectura Limpia, los casos de uso dependen de esta abstracción,
/// desvinculándose de la tecnología concreta de base de datos (In-Memory, Postgres, etc.).
#[async_trait]
pub trait UserRepository: Send + Sync {
    /// Guarda un nuevo usuario en el almacén de datos.
    async fn create(&self, user: User) -> Result<User, UserError>;

    /// Obtiene un usuario por su UUID.
    async fn find_by_id(&self, id: Uuid) -> Result<Option<User>, UserError>;

    /// Obtiene un usuario por su correo electrónico.
    async fn find_by_email(&self, email: &str) -> Result<Option<User>, UserError>;

    /// Devuelve el listado de todos los usuarios.
    async fn list_all(&self) -> Result<Vec<User>, UserError>;

    /// Actualiza los datos de un usuario existente.
    async fn update(&self, user: User) -> Result<User, UserError>;

    /// Elimina un usuario según su UUID.
    async fn delete(&self, id: Uuid) -> Result<(), UserError>;
}
