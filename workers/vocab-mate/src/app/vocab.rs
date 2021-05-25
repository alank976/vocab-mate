use chrono::prelude::*;
use serde::Serialize;

#[derive(Debug, Serialize)]
pub struct Vocab {
    pub id: Option<String>,
    pub word: String,
    pub(crate) part_of_speech: PartOfSpeech,
    pub definition: String,
    pub examples: Option<Vec<String>>,
    pub synonyms: Option<Vec<String>>,
    pub antonyms: Option<Vec<String>>,
    pub last_updated: Option<DateTime<Local>>,
}

impl Vocab {
    pub fn new(
        id: Option<String>,
        word: String,
        part_of_speech: PartOfSpeech,
        definition: String,
        examples: Option<Vec<String>>,
        synonyms: Option<Vec<String>>,
        antonyms: Option<Vec<String>>,
        last_updated: Option<DateTime<Local>>,
    ) -> Self {
        Self {
            id,
            word,
            part_of_speech,
            definition,
            examples,
            synonyms,
            antonyms,
            last_updated,
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

impl PartOfSpeech {
    pub fn from(s: String) -> Option<Self> {
        match s.to_lowercase().as_str() {
            "noun" => Some(Self::Noun),
            "verb" => Some(Self::Verb),
            "adjective" => Some(Self::Adjective),
            "adverb" => Some(Self::Adverb),
            _ => None,
        }
    }
}
