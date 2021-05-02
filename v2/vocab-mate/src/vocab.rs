use chrono::prelude::*;
use serde::Serialize;

#[derive(Debug, Serialize)]
pub struct Vocab {
    pub id: String,
    pub word: String,
    pub(crate) part_of_speech: PartOfSpeech,
    pub definition: String,
    pub examples: Vec<String>,
    pub synonyms: Vec<String>,
    pub antonyms: Vec<String>,
    pub last_updated: DateTime<Local>,
}

impl Vocab {
    pub fn new(
        id: String,
        word: String,
        part_of_speech: PartOfSpeech,
        definition: String,
        // examples: Vec<String>,
        // synonyms: Vec<String>,
        // antonyms: Vec<String>,
        // last_updated: DateTime<Local>,
    ) -> Self {
        Self {
            id,
            word,
            part_of_speech,
            definition,
            examples: Vec::new(),
            synonyms: Vec::new(),
            antonyms: Vec::new(),
            last_updated: Local::now(),
        }
    }
}

#[derive(Debug, Serialize)]
pub enum PartOfSpeech {
    Noun,
    Verb,
    Adjective,
    Adverb,
}
