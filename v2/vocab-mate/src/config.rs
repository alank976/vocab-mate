use std::env;

use serde::Deserialize;

use config::{Config, ConfigError, File};

#[derive(Debug, Deserialize)]
pub struct FaunadbConfig {
    pub url: String,
    pub api_key: String,
}

#[derive(Debug, Deserialize)]
pub struct RapidApiConfig {
    pub wordsapi_url: String,
    pub api_key_header: String,
    pub api_key: String,
}

#[derive(Debug, Deserialize)]
pub struct Configs {
    pub faunadb: FaunadbConfig,
    pub rapidapi: RapidApiConfig,
}

impl Configs {
    pub fn new() -> Result<Self, ConfigError> {
        let mut c = Config::new();
        c.merge(File::with_name("config/default"))?;
        let env = env::var("RUN_MODE").unwrap_or_else(|_| "dev".into());
        c.merge(File::with_name(&format!("config/{}", env)).required(false))?;
        c.try_into()
    }
}
