use std::{env, net::IpAddr, net::SocketAddr};

use thiserror::Error;

const DEFAULT_SERVER_HOST: &str = "127.0.0.1";
const DEFAULT_SERVER_PORT: &str = "3000";
const DEFAULT_DATABASE_MAX_CONNECTIONS: &str = "5";

#[derive(Clone)]
pub struct Settings {
    pub database_url: String,
    pub server_address: SocketAddr,
    pub database_max_connections: u32,
}

#[derive(Debug, Error, PartialEq, Eq)]
pub enum SettingsError {
    #[error("Falta la variable de entorno obligatoria {0}")]
    MissingVariable(&'static str),

    #[error("Valor inválido para {key}: '{value}' ({reason})")]
    InvalidVariable {
        key: &'static str,
        value: String,
        reason: &'static str,
    },
}

impl Settings {
    pub fn from_env() -> Result<Self, SettingsError> {
        Self::from_provider(|key| env::var(key).ok())
    }

    fn from_provider<F>(mut get: F) -> Result<Self, SettingsError>
    where
        F: FnMut(&str) -> Option<String>,
    {
        let database_url = get("DATABASE_URL")
            .filter(|value| !value.trim().is_empty())
            .ok_or(SettingsError::MissingVariable("DATABASE_URL"))?;

        let host_value = get("SERVER_HOST").unwrap_or_else(|| DEFAULT_SERVER_HOST.to_string());
        let server_host =
            host_value
                .parse::<IpAddr>()
                .map_err(|_| SettingsError::InvalidVariable {
                    key: "SERVER_HOST",
                    value: host_value,
                    reason: "se esperaba una dirección IP",
                })?;

        let port_value = get("SERVER_PORT").unwrap_or_else(|| DEFAULT_SERVER_PORT.to_string());
        let server_port =
            port_value
                .parse::<u16>()
                .map_err(|_| SettingsError::InvalidVariable {
                    key: "SERVER_PORT",
                    value: port_value,
                    reason: "se esperaba un puerto entre 0 y 65535",
                })?;

        let max_connections_value = get("DATABASE_MAX_CONNECTIONS")
            .unwrap_or_else(|| DEFAULT_DATABASE_MAX_CONNECTIONS.to_string());
        let database_max_connections =
            max_connections_value
                .parse::<u32>()
                .map_err(|_| SettingsError::InvalidVariable {
                    key: "DATABASE_MAX_CONNECTIONS",
                    value: max_connections_value.clone(),
                    reason: "se esperaba un entero positivo",
                })?;

        if database_max_connections == 0 {
            return Err(SettingsError::InvalidVariable {
                key: "DATABASE_MAX_CONNECTIONS",
                value: max_connections_value,
                reason: "debe ser mayor que cero",
            });
        }

        Ok(Self {
            database_url,
            server_address: SocketAddr::new(server_host, server_port),
            database_max_connections,
        })
    }
}

#[cfg(test)]
mod tests {
    use std::collections::HashMap;

    use super::{Settings, SettingsError};

    fn settings_from(values: &[(&str, &str)]) -> Result<Settings, SettingsError> {
        let values = values
            .iter()
            .map(|(key, value)| ((*key).to_string(), (*value).to_string()))
            .collect::<HashMap<_, _>>();

        Settings::from_provider(|key| values.get(key).cloned())
    }

    #[test]
    fn requires_database_url() {
        let result = settings_from(&[]);

        assert!(matches!(
            result,
            Err(SettingsError::MissingVariable("DATABASE_URL"))
        ));
    }

    #[test]
    fn uses_defaults_for_optional_settings() {
        let settings = settings_from(&[("DATABASE_URL", "postgres://localhost/app")])
            .expect("the settings should be valid");

        assert_eq!(settings.server_address.to_string(), "127.0.0.1:3000");
        assert_eq!(settings.database_max_connections, 5);
    }

    #[test]
    fn accepts_custom_settings() {
        let settings = settings_from(&[
            ("DATABASE_URL", "postgres://localhost/app"),
            ("SERVER_HOST", "0.0.0.0"),
            ("SERVER_PORT", "8080"),
            ("DATABASE_MAX_CONNECTIONS", "10"),
        ])
        .expect("the settings should be valid");

        assert_eq!(settings.server_address.to_string(), "0.0.0.0:8080");
        assert_eq!(settings.database_max_connections, 10);
    }

    #[test]
    fn rejects_invalid_server_port() {
        let result = settings_from(&[
            ("DATABASE_URL", "postgres://localhost/app"),
            ("SERVER_PORT", "invalid"),
        ]);

        assert!(matches!(
            result,
            Err(SettingsError::InvalidVariable {
                key: "SERVER_PORT",
                ..
            })
        ));
    }

    #[test]
    fn rejects_zero_database_connections() {
        let result = settings_from(&[
            ("DATABASE_URL", "postgres://localhost/app"),
            ("DATABASE_MAX_CONNECTIONS", "0"),
        ]);

        assert!(matches!(
            result,
            Err(SettingsError::InvalidVariable {
                key: "DATABASE_MAX_CONNECTIONS",
                ..
            })
        ));
    }
}
