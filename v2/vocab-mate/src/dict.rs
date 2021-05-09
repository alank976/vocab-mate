use crate::{fauna::FaunaDbClient, rapidapi::WordsApiClient, vocab::*};
use anyhow::Result;
use async_trait::async_trait;
use log::debug;

#[async_trait]
pub trait Dict {
    fn look_up(&self, vocab: String) -> Result<Vec<Vocab>>;
    async fn async_lookup(&self, vocab: String) -> Result<Vec<Vocab>>;
}

#[derive(Clone)]
pub struct DictImpl {
    fauna_client: FaunaDbClient,
    words_api_client: WordsApiClient,
}

impl DictImpl {
    pub fn new(fauna_client: FaunaDbClient, words_api_client: WordsApiClient) -> Self {
        Self {
            fauna_client,
            words_api_client,
        }
    }
}
#[async_trait]
impl Dict for DictImpl {
    fn look_up(&self, vocab: String) -> Result<Vec<Vocab>> {
        let mut results = self.fauna_client.look_up(vocab)?;
        let mut first = results.remove(0);
        first.word = first.word + "-test";
        let created = self.fauna_client.create(first).map(|x| vec![x])?;
        let id = created
            .first()
            .map(|first| first.id.clone())
            .flatten()
            .expect("no first ID");
        self.fauna_client.delete(id)?;
        Ok(created)
    }

    async fn async_lookup(&self, vocab: String) -> Result<Vec<Vocab>> {
        debug!("reach dict impl");
        self.words_api_client.async_lookup(vocab).await
        // self.fauna_client.async_lookup(vocab).await
    }
}
