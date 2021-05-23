use chrono;
use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize)]
pub struct FaunadbConfig {
    pub url: String,
    pub api_key: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct RapidApiConfig {
    pub wordsapi_url: String,
    pub api_key_header: String,
    pub api_key: String,
}

#[derive(Debug, Serialize, Deserialize)]
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

#[derive(Debug, Serialize, Deserialize)]
pub struct Configs {
    pub faunadb: FaunadbConfig,
    pub rapidapi: RapidApiConfig,
    pub vocab_mate: VocabMateConfig,
}
