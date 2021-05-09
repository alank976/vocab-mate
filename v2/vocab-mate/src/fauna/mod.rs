use crate::prelude::*;
use anyhow::{anyhow, Result};
use graphql_client::{GraphQLQuery, QueryBody, Response};
use reqwest;
use serde::{de::DeserializeOwned, Serialize};

mod graphql_gen;

#[derive(Clone)]
pub struct FaunaDbClient {
    url: String,
    api_key: String,
    http_client: reqwest::blocking::Client,
}

impl FaunaDbClient {
    pub fn new(url: String, api_key: String) -> Self {
        Self {
            url,
            api_key,
            http_client: reqwest::blocking::Client::new(),
        }
    }

    pub fn create(&self, vocab: Vocab) -> Result<Vocab> {
        let word = vocab.word.clone();
        let request_body = graphql_gen::CreateOne::build_query(vocab.into());
        let response_body: Response<graphql_gen::create_one::ResponseData> =
            self.request_graphql(request_body)?;
        response_body
            .data
            .map(|data| data.create_vocab.into())
            .ok_or_else(|| anyhow!("successful create but no ID returned for {:?}", word))
    }

    pub fn delete(&self, id: String) -> Result<()> {
        let request_body =
            graphql_gen::DeleteOne::build_query(graphql_gen::delete_one::Variables { id });
        let _response_body: Response<graphql_gen::delete_one::ResponseData> =
            self.request_graphql(request_body)?;
        Ok(())
    }

    fn request_graphql<Request: Serialize, Response: DeserializeOwned>(
        &self,
        body: QueryBody<Request>,
    ) -> Result<Response> {
        let response = self
            .http_client
            .post(self.url.clone())
            .bearer_auth(self.api_key.clone())
            .json(&body)
            .send()?;
        response.error_for_status_ref()?;
        let response_body: Response = response.json()?;
        Ok(response_body)
    }
}

impl Dict for FaunaDbClient {
    fn look_up(&self, vocab: String) -> Result<Vec<Vocab>> {
        let request_body =
            graphql_gen::FindQuery::build_query(graphql_gen::find_query::Variables { word: vocab });
        let response_body: Response<graphql_gen::find_query::ResponseData> =
            self.request_graphql(request_body)?;
        let vocabs: Vec<Vocab> = response_body
            .data
            .map(|resp_d| {
                let inner_vec = resp_d.find_vocabs_by_word.data;
                inner_vec.into_iter().map(|data| data.into()).collect()
            })
            .unwrap_or(Vec::new());
        Ok(vocabs)
    }
}

mod dto_mapping {
    use super::graphql_gen::{create_one, find_query};
    use crate::prelude::*;
    use chrono::prelude::*;

    fn to_local_datetime(micro_sec: i64) -> DateTime<Local> {
        Local.timestamp_nanos(micro_sec * 1_000)
    }

    impl From<Vocab> for create_one::Variables {
        fn from(vocab: Vocab) -> Self {
            create_one::Variables {
                input: create_one::VocabInput {
                    word: vocab.word.clone(),
                    part_of_speech: vocab.part_of_speech.into(),
                    definition: vocab.definition,
                    examples: Some(vocab.examples),
                    synonyms: Some(vocab.synonyms),
                    antonyms: Some(vocab.antonyms),
                },
            }
        }
    }

    impl From<create_one::CreateOneCreateVocab> for Vocab {
        fn from(data: create_one::CreateOneCreateVocab) -> Self {
            let data = data.common_fields;
            Self::new(
                data.id,
                data.word,
                data.part_of_speech.into(),
                data.definition,
                to_local_datetime(data.ts),
            )
        }
    }

    impl From<find_query::FindQueryFindVocabsByWordData> for Vocab {
        fn from(data: find_query::FindQueryFindVocabsByWordData) -> Self {
            let data = data.common_fields;
            Self::new(
                data.id,
                data.word,
                data.part_of_speech.into(),
                data.definition,
                to_local_datetime(data.ts),
            )
        }
    }

    impl From<create_one::PartOfSpeech> for PartOfSpeech {
        fn from(p: create_one::PartOfSpeech) -> Self {
            match p {
                create_one::PartOfSpeech::Noun => PartOfSpeech::Noun,
                create_one::PartOfSpeech::Verb => PartOfSpeech::Verb,
                create_one::PartOfSpeech::Adjective => PartOfSpeech::Adjective,
                create_one::PartOfSpeech::Adverb => PartOfSpeech::Adverb,
                create_one::PartOfSpeech::Other(_) => todo!("implement unknown"),
            }
        }
    }
    impl From<PartOfSpeech> for create_one::PartOfSpeech {
        fn from(p: PartOfSpeech) -> Self {
            match p {
                PartOfSpeech::Noun => create_one::PartOfSpeech::Noun,
                PartOfSpeech::Verb => create_one::PartOfSpeech::Verb,
                PartOfSpeech::Adjective => create_one::PartOfSpeech::Adjective,
                PartOfSpeech::Adverb => create_one::PartOfSpeech::Adverb,
            }
        }
    }

    impl From<find_query::PartOfSpeech> for PartOfSpeech {
        fn from(p: find_query::PartOfSpeech) -> Self {
            match p {
                find_query::PartOfSpeech::Noun => PartOfSpeech::Noun,
                find_query::PartOfSpeech::Verb => PartOfSpeech::Verb,
                find_query::PartOfSpeech::Adjective => PartOfSpeech::Adjective,
                find_query::PartOfSpeech::Adverb => PartOfSpeech::Adverb,
                find_query::PartOfSpeech::Other(_) => todo!("implement unknown"),
            }
        }
    }
}
