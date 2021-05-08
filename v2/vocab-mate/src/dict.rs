use crate::{fauna::FaunaDbClient, vocab::*};
use anyhow::Result;

pub trait Dict {
    fn look_up(&self, vocab: String) -> Result<Vec<Vocab>>;
}

#[derive(Clone)]
pub struct DictImpl {
    fauna_client: FaunaDbClient,
}

impl DictImpl {
    pub fn new(fauna_client: FaunaDbClient) -> Self {
        Self { fauna_client }
    }
}

impl Dict for DictImpl {
    fn look_up(&self, vocab: String) -> Result<Vec<Vocab>> {
        let mut results = self.fauna_client.look_up(vocab)?;
        let mut first = results.remove(0);
        first.word = first.word + "-test";
        self.fauna_client.create(first).map(|x| vec![x])
    }
}
