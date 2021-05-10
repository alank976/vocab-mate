use crate::{fauna::FaunaDbClient, rapidapi::WordsApiClient, vocab::*};
use anyhow::Result;
use async_trait::async_trait;
use chrono::{Duration, Local};
use log::info;

#[async_trait]
pub trait Dict {
    fn look_up(&self, vocab: String) -> Result<Vec<Vocab>>;
    async fn async_lookup(&self, vocab: String) -> Result<Vec<Vocab>>;
}

#[derive(Clone)]
pub struct DictImpl {
    fauna_client: FaunaDbClient,
    words_api_client: WordsApiClient,
    expiry: Duration,
}

impl DictImpl {
    pub fn new(
        fauna_client: FaunaDbClient,
        words_api_client: WordsApiClient,
        expiry: Duration,
    ) -> Self {
        Self {
            fauna_client,
            words_api_client,
            expiry,
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
        let stored = self.fauna_client.async_lookup(vocab.clone()).await?;
        info!("stored={:?}", stored);
        let is_empty_or_expired = stored.is_empty()
            || stored
                .iter()
                .filter_map(|v| v.last_updated)
                .min()
                .filter(|oldest| {
                    info!("oldest time={}", oldest);
                    Local::now().signed_duration_since(oldest.clone()) > self.expiry
                })
                .is_some();
        if is_empty_or_expired {
            info!("lookup words api now");
            for v in stored {
                if let Some(id) = v.id {
                    self.fauna_client.delete(id)?;
                }
            }
            self.words_api_client
                .async_lookup(vocab)
                .await?
                .into_iter()
                .map(|v| self.fauna_client.create(v))
                .collect()
        } else {
            Ok(stored)
        }
    }
}
