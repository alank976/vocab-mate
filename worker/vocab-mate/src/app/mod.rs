use crate::configs::Configs;

pub mod api;
mod dict;
mod fauna;
mod rapidapi;
mod vocab;

use crate::app::prelude::*;
use dict::{Dict, DictImpl};

pub mod prelude {
    pub use super::dict::*;
    pub use super::vocab::*;
}

pub async fn lookup(configs: Configs, vocab: String) -> Vec<Vocab> {
    let context = create_app_context(application_configs);
    let vocabs = context
        .dict
        .async_lookup(vocab)
        .await
        .expect("failed to async lookup vocab");
    vocabs
}

fn create_app_context(config: Configs) -> AppContext<DictImpl> {
    let fauna_client = fauna::FaunaDbClient::new(config.faunadb.url, config.faunadb.api_key);
    let words_api_client = rapidapi::WordsApiClient::new(
        config.rapidapi.wordsapi_url,
        config.rapidapi.api_key_header,
        config.rapidapi.api_key,
    );
    let dict_impl = DictImpl::new(fauna_client, words_api_client, config.vocab_mate.expiry());
    AppContext::new(dict_impl)
}

// TODO: optimize not to clone
#[derive(Clone)]
pub struct AppContext<D>
where
    D: Dict,
{
    pub dict: D,
}

impl<D: Dict + Clone> AppContext<D> {
    pub fn new(dict: D) -> Self {
        Self { dict }
    }
}
