use async_trait::async_trait;
use std::collections::HashMap;
use std::sync::{Arc, RwLock};
use uuid::Uuid;

use crate::domain::{User, UserError, UserRepository};

/// Adaptador de Persistencia en Memoria.
/// Implementa el puerto `UserRepository` utilizando un HashMap thread-safe protegido por RwLock.
#[allow(dead_code)]
#[derive(Clone)]
pub struct InMemoryUserRepository {

    store: Arc<RwLock<HashMap<Uuid, User>>>,
}

#[allow(dead_code)]
impl InMemoryUserRepository {

    pub fn new() -> Self {
        Self {
            store: Arc::new(RwLock::new(HashMap::new())),
        }
    }
}

#[async_trait]
impl UserRepository for InMemoryUserRepository {
    async fn create(&self, user: User) -> Result<User, UserError> {
        let mut store = self
            .store
            .write()
            .map_err(|e| UserError::RepositoryError(e.to_string()))?;

        store.insert(user.id, user.clone());
        Ok(user)
    }

    async fn find_by_id(&self, id: Uuid) -> Result<Option<User>, UserError> {
        let store = self
            .store
            .read()
            .map_err(|e| UserError::RepositoryError(e.to_string()))?;

        Ok(store.get(&id).cloned())
    }

    async fn find_by_email(&self, email: &str) -> Result<Option<User>, UserError> {
        let store = self
            .store
            .read()
            .map_err(|e| UserError::RepositoryError(e.to_string()))?;

        Ok(store.values().find(|u| u.email == email).cloned())
    }

    async fn list_all(&self) -> Result<Vec<User>, UserError> {
        let store = self
            .store
            .read()
            .map_err(|e| UserError::RepositoryError(e.to_string()))?;

        Ok(store.values().cloned().collect())
    }

    async fn update(&self, user: User) -> Result<User, UserError> {
        let mut store = self
            .store
            .write()
            .map_err(|e| UserError::RepositoryError(e.to_string()))?;

        if store.contains_key(&user.id) {
            store.insert(user.id, user.clone());
            Ok(user)
        } else {
            Err(UserError::NotFound(user.id))
        }
    }

    async fn delete(&self, id: Uuid) -> Result<(), UserError> {
        let mut store = self
            .store
            .write()
            .map_err(|e| UserError::RepositoryError(e.to_string()))?;

        if store.remove(&id).is_some() {
            Ok(())
        } else {
            Err(UserError::NotFound(id))
        }
    }
}
