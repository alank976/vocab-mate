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
pub struct VocabMateConfig {
    expiry: String,
}

impl VocabMateConfig {
    pub fn expiry(&self) -> chrono::Duration {
        let count = self.expiry.len();
        let unit = self
            .expiry
            .chars()
            .last()
            .expect("Invalid expiry duration format");
        let n: String = self.expiry.chars().into_iter().take(count - 1).collect();
        let n = n
            .parse::<i64>()
            .expect("failed to parse expiry duration to i64");
        match unit {
            'd' => chrono::Duration::days(n),
            'h' => chrono::Duration::hours(n),
            'm' => chrono::Duration::minutes(n),
            's' => chrono::Duration::seconds(n),
            _ => panic!("{} duration unit is not supported", unit),
        }
    }
}

#[derive(Debug, Deserialize)]
pub struct Configs {
    pub faunadb: FaunadbConfig,
    pub rapidapi: RapidApiConfig,
    pub vocab_mate: VocabMateConfig,
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
