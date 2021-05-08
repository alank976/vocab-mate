use crate::prelude::*;

pub struct WordsApiClient {}

impl Dict for WordsApiClient {
    fn look_up(&self, _vocab: String) -> anyhow::Result<Vec<Vocab>> {
        todo!()
    }
}
