use std::sync::Arc;

use axum_server::application::UserService;
use axum_server::domain::{CreateUserPayload, UserError};
use axum_server::infrastructure::InMemoryUserRepository;


#[tokio::test]
async fn test_create_user_success() {
    let repo = Arc::new(InMemoryUserRepository::new());
    let service = UserService::new(repo);

    let payload = CreateUserPayload {
        name: "Carlos Gómez".to_string(),
        email: "carlos@example.com".to_string(),
    };

    let result = service.create_user(payload).await;
    assert!(result.is_ok());

    let user = result.unwrap();
    assert_eq!(user.name, "Carlos Gómez");
    assert_eq!(user.email, "carlos@example.com");
    assert!(user.active);
}

#[tokio::test]
async fn test_create_user_invalid_email() {
    let repo = Arc::new(InMemoryUserRepository::new());
    let service = UserService::new(repo);

    let payload = CreateUserPayload {
        name: "Carlos Gómez".to_string(),
        email: "invalid-email".to_string(),
    };

    let result = service.create_user(payload).await;
    assert!(matches!(result, Err(UserError::ValidationError(_))));
}

#[tokio::test]
async fn test_prevent_duplicate_email() {
    let repo = Arc::new(InMemoryUserRepository::new());
    let service = UserService::new(repo);

    let payload1 = CreateUserPayload {
        name: "Usuario 1".to_string(),
        email: "repetido@example.com".to_string(),
    };
    let payload2 = CreateUserPayload {
        name: "Usuario 2".to_string(),
        email: "repetido@example.com".to_string(),
    };

    let _ = service.create_user(payload1).await.unwrap();
    let result = service.create_user(payload2).await;

    assert!(matches!(result, Err(UserError::EmailAlreadyExists(_))));
}
