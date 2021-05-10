use crate::prelude::*;
use anyhow::anyhow;
use async_trait::async_trait;
use log::{debug, error};

#[derive(Clone)]
pub struct WordsApiClient {
    url: String,
    api_key_header: String,
    api_key_value: String,
}
impl WordsApiClient {
    pub(crate) fn new(url: String, api_key_header: String, api_key_value: String) -> Self {
        Self {
            url,
            api_key_header,
            api_key_value,
        }
    }
}

#[async_trait]
impl Dict for WordsApiClient {
    async fn async_lookup(&self, vocab: String) -> anyhow::Result<Vec<Vocab>> {
        let mut response = surf::get(format!("{}/words/{}", self.url, vocab))
            .header(self.api_key_header.as_str(), self.api_key_value.as_str())
            .await
            .map_err(|e| anyhow!(e))?;
        if let Some(values) = response.header("X-RateLimit-requests-Remaining") {
            let limit: u32 = values
                .last()
                .as_str()
                .parse()
                .expect("X-RateLimit-requests-Remaining has non numeric value");
            debug!("API usage limit remaining={}", limit);
            if limit < 10 {
                error!("MUST STOP USING THE API FOR THE SAKE OF $$$$$");
            }
        };
        let words_api_response: dto::WordsApiResponse =
            response.body_json().await.map_err(|e| anyhow!(e))?;
        let word = words_api_response.word;
        Ok(words_api_response
            .results
            .into_iter()
            .map(|result| {
                let mut vocab: Vocab = result.into();
                vocab.word = word.clone();
                vocab
            })
            .collect())
    }
}

mod dto {
    use crate::prelude::{PartOfSpeech, Vocab};
    use serde::{Deserialize, Serialize};

    impl From<WordsApiResult> for Vocab {
        fn from(result: WordsApiResult) -> Self {
            Self::new(
                None,
                "".into(),
                PartOfSpeech::from(result.part_of_speech.clone())
                    .expect(format!("Unknown part of speech {}", result.part_of_speech).as_str()),
                result.definition,
                result.examples,
                result.synonyms,
                result.antonyms,
                None,
            )
        }
    }

    #[derive(Serialize, Deserialize)]
    pub(crate) struct WordsApiResponse {
        pub(crate) word: String,
        pub(crate) results: Vec<WordsApiResult>,
    }

    #[derive(Serialize, Deserialize)]
    pub(crate) struct WordsApiResult {
        definition: String,
        #[serde(rename = "partOfSpeech")]
        part_of_speech: String,

        synonyms: Option<Vec<String>>,
        #[serde(rename = "typeOf")]
        type_of: Option<Vec<String>>,
        #[serde(rename = "hasTypes")]
        has_types: Option<Vec<String>>,
        derivation: Option<Vec<String>>,
        examples: Option<Vec<String>>,
        #[serde(rename = "similarTo")]
        similar_to: Option<Vec<String>>,
        antonyms: Option<Vec<String>>,
    }
}
