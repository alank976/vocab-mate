use crate::prelude::*;

use graphql_client::{GraphQLQuery, QueryBody, Response};
use reqwest;
use serde::Serialize;

#[derive(GraphQLQuery)]
#[graphql(
    schema_path = "graphql/schema.graphql",
    query_path = "graphql/query.graphql",
    response_derives = "Debug"
)]
pub struct FindOne;

type Long = u128;

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

    fn request_graphql(
        &self,
        body: QueryBody<impl Serialize>,
    ) -> anyhow::Result<reqwest::blocking::Response> {
        let response = self
            .http_client
            .post(self.url.clone())
            .bearer_auth(self.api_key.clone())
            .json(&body)
            .send()?;
        response.error_for_status_ref()?;
        Ok(response)
    }
}

impl Dict for FaunaDbClient {
    fn look_up(&self, vocab: String) -> anyhow::Result<Vec<Vocab>> {
        let request_body = FindOne::build_query(find_one::Variables { word: vocab });
        let response = self.request_graphql(request_body)?;
        let response_body: Response<find_one::ResponseData> = response.json()?;
        let vocabs: Vec<Vocab> = response_body
            .data
            .map(|resp_d| {
                let inner_vec = resp_d.find_vocabs_by_word.data;
                inner_vec
                    .into_iter()
                    .map(|data: find_one::FindOneFindVocabsByWordData| {
                        Vocab::new(
                            data.id,
                            data.word,
                            convert_part_of_speech(data.part_of_speech),
                            data.definition,
                        )
                    })
                    .collect()
            })
            .unwrap_or(Vec::new());
        Ok(vocabs)
    }
}

fn convert_part_of_speech(x: find_one::PartOfSpeech) -> PartOfSpeech {
    match x {
        find_one::PartOfSpeech::Noun => PartOfSpeech::Noun,
        find_one::PartOfSpeech::Verb => PartOfSpeech::Verb,
        find_one::PartOfSpeech::Adjective => PartOfSpeech::Adjective,
        find_one::PartOfSpeech::Adverb => PartOfSpeech::Adverb,
        find_one::PartOfSpeech::Other(_) => todo!("implement unknown"),
    }
}
