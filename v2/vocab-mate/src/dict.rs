use crate::vocab::*;
use anyhow;

pub trait Dict {
    fn look_up(&self, vocab: String) -> anyhow::Result<Vec<Vocab>>;
}
