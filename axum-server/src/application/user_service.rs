use std::sync::Arc;
use uuid::Uuid;

use crate::domain::{CreateUserPayload, UpdateUserPayload, User, UserError, UserRepository};

/// Servicio de la Capa de Aplicación que contiene los Casos de Uso del Usuario.
/// Orquesta la lógica de negocio y se comunica con la capa de infraestructura
/// a través de la abstracción `UserRepository`.
pub struct UserService {
    repo: Arc<dyn UserRepository>,
}

impl UserService {
    /// Inicializa una nueva instancia del servicio inyectando el repositorio.
    pub fn new(repo: Arc<dyn UserRepository>) -> Self {
        Self { repo }
    }

    /// Caso de Uso: Crear un nuevo usuario
    pub async fn create_user(&self, payload: CreateUserPayload) -> Result<User, UserError> {
        // Validaciones de negocio simples
        if payload.name.trim().is_empty() {
            return Err(UserError::ValidationError(
                "El nombre del usuario no puede estar vacío".to_string(),
            ));
        }

        if !payload.email.contains('@') {
            return Err(UserError::ValidationError(
                "Formato de correo electrónico inválido".to_string(),
            ));
        }

        // Verificar si el correo ya existe
        if self.repo.find_by_email(&payload.email).await?.is_some() {
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

    /// Caso de Uso: Buscar usuario por ID
    pub async fn get_user_by_id(&self, id: Uuid) -> Result<User, UserError> {
        self.repo
            .find_by_id(id)
            .await?
            .ok_or(UserError::NotFound(id))
    }

    /// Caso de Uso: Listar todos los usuarios
    pub async fn list_users(&self) -> Result<Vec<User>, UserError> {
        self.repo.list_all().await
    }

    /// Caso de Uso: Actualizar un usuario existente
    pub async fn update_user(
        &self,
        id: Uuid,
        payload: UpdateUserPayload,
    ) -> Result<User, UserError> {
        let mut user = self.get_user_by_id(id).await?;

        if let Some(name) = payload.name {
            if name.trim().is_empty() {
                return Err(UserError::ValidationError(
                    "El nombre no puede estar vacío".to_string(),
                ));
            }
            user.name = name;
        }

        if let Some(email) = payload.email {
            if !email.contains('@') {
                return Err(UserError::ValidationError(
                    "Formato de email inválido".to_string(),
                ));
            }
            // Si el correo cambia, validar que no esté ocupado
            if email != user.email && self.repo.find_by_email(&email).await?.is_some() {
                return Err(UserError::EmailAlreadyExists(email));
            }
            user.email = email;
        }

        if let Some(active) = payload.active {
            user.active = active;
        }

        self.repo.update(user).await
    }

    /// Caso de Uso: Eliminar un usuario por ID
    pub async fn delete_user(&self, id: Uuid) -> Result<(), UserError> {
        // Verificar que existe antes de intentar eliminar
        let _ = self.get_user_by_id(id).await?;
        self.repo.delete(id).await
    }
}
