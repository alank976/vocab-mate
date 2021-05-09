use std::io::Result;

use actix_web::{App, HttpServer};
use dict::Dict;

mod api;
mod config;
mod dict;
mod fauna;
mod rapidapi;
mod vocab;

pub mod prelude {
    pub use crate::config::*;
    pub use crate::dict::*;
    pub use crate::vocab::*;
}

#[actix_web::main]
async fn main() -> Result<()> {
    env_logger::init();
    HttpServer::new(|| {
        App::new()
            .data_factory(create_app_context)
            .service(api::get_vocab)
    })
    .bind("127.0.0.1:8080")?
    .run()
    .await
}

async fn create_app_context() -> Result<AppContext<dict::DictImpl>> {
    let config =
        config::Configs::new().map_err(|x| std::io::Error::new(std::io::ErrorKind::Other, x))?;
    let fauna_client = fauna::FaunaDbClient::new(config.faunadb.url, config.faunadb.api_key);
    let words_api_client = rapidapi::WordsApiClient::new(
        config.rapidapi.wordsapi_url,
        config.rapidapi.api_key_header,
        config.rapidapi.api_key,
    );
    let dict_impl = dict::DictImpl::new(fauna_client, words_api_client, config.vocab_mate.expiry());
    Ok(AppContext::new(dict_impl))
}

// TODO: optimize not to clone
#[derive(Clone)]
pub struct AppContext<D>
where
    D: Dict,
{
    dict: D,
}

impl<D: Dict + Clone> AppContext<D> {
    pub fn new(dict: D) -> Self {
        Self { dict }
    }
}
