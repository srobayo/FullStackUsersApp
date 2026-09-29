use async_trait::async_trait;
use sqlx::{FromRow, PgPool};
use uuid::Uuid;

use crate::domain::{User, UserError, UserRepository};

/// Modelo de mapeo interno de SQLx para la tabla `users`
#[derive(Debug, FromRow)]
struct SqlxUser {
    id: Uuid,
    name: String,
    email: String,
    active: bool,
}

impl From<SqlxUser> for User {
    fn from(u: SqlxUser) -> Self {
        User {
            id: u.id,
            name: u.name,
            email: u.email,
            active: u.active,
        }
    }
}

/// Adaptador de Persistencia para PostgreSQL.
/// Implementa el puerto `UserRepository` conectándose a la base de datos a través de `sqlx::PgPool`.
#[derive(Clone)]
pub struct PostgresUserRepository {
    pool: PgPool,
}

impl PostgresUserRepository {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
}

#[async_trait]
impl UserRepository for PostgresUserRepository {
    async fn create(&self, user: User) -> Result<User, UserError> {
        let record = sqlx::query_as::<_, SqlxUser>(
            r#"
            INSERT INTO users (id, name, email, active)
            VALUES ($1, $2, $3, $4)
            RETURNING id, name, email, active
            "#,
        )
        .bind(user.id)
        .bind(&user.name)
        .bind(&user.email)
        .bind(user.active)
        .fetch_one(&self.pool)
        .await
        .map_err(|e| match e {
            sqlx::Error::Database(db_err) if db_err.is_unique_violation() => {
                UserError::EmailAlreadyExists(user.email.clone())
            }
            other => UserError::RepositoryError(other.to_string()),
        })?;

        Ok(record.into())
    }

    async fn find_by_id(&self, id: Uuid) -> Result<Option<User>, UserError> {
        let record = sqlx::query_as::<_, SqlxUser>(
            r#"
            SELECT id, name, email, active
            FROM users
            WHERE id = $1
            "#,
        )
        .bind(id)
        .fetch_optional(&self.pool)
        .await
        .map_err(|e| UserError::RepositoryError(e.to_string()))?;

        Ok(record.map(User::from))
    }

    async fn find_by_email(&self, email: &str) -> Result<Option<User>, UserError> {
        let record = sqlx::query_as::<_, SqlxUser>(
            r#"
            SELECT id, name, email, active
            FROM users
            WHERE email = $1
            "#,
        )
        .bind(email)
        .fetch_optional(&self.pool)
        .await
        .map_err(|e| UserError::RepositoryError(e.to_string()))?;

        Ok(record.map(User::from))
    }

    async fn list_all(&self) -> Result<Vec<User>, UserError> {
        let records = sqlx::query_as::<_, SqlxUser>(
            r#"
            SELECT id, name, email, active
            FROM users
            ORDER BY name ASC
            "#,
        )
        .fetch_all(&self.pool)
        .await
        .map_err(|e| UserError::RepositoryError(e.to_string()))?;

        Ok(records.into_iter().map(User::from).collect())
    }

    async fn update(&self, user: User) -> Result<User, UserError> {
        let record = sqlx::query_as::<_, SqlxUser>(
            r#"
            UPDATE users
            SET name = $1, email = $2, active = $3
            WHERE id = $4
            RETURNING id, name, email, active
            "#,
        )
        .bind(&user.name)
        .bind(&user.email)
        .bind(user.active)
        .bind(user.id)
        .fetch_optional(&self.pool)
        .await
        .map_err(|e| match e {
            sqlx::Error::Database(db_err) if db_err.is_unique_violation() => {
                UserError::EmailAlreadyExists(user.email.clone())
            }
            other => UserError::RepositoryError(other.to_string()),
        })?;

        record.map(User::from).ok_or(UserError::NotFound(user.id))
    }

    async fn delete(&self, id: Uuid) -> Result<(), UserError> {
        let result = sqlx::query(
            r#"
            DELETE FROM users
            WHERE id = $1
            "#,
        )
        .bind(id)
        .execute(&self.pool)
        .await
        .map_err(|e| UserError::RepositoryError(e.to_string()))?;

        if result.rows_affected() == 0 {
            Err(UserError::NotFound(id))
        } else {
            Ok(())
        }
    }
}
