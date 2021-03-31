package github.io.vocabmate.infrastructure

import github.io.vocabmate.domain.words.Vocab
import io.micronaut.core.annotation.Creator
import io.micronaut.core.annotation.Introspected

@Introspected
data class WordsResult(
    val definition: String,
    val partOfSpeech: String,
    var synonyms: List<String>,
    var typeOf: List<String>,
    var hasTypes: List<String>,
    var derivation: List<String>,
    var examples: List<String>,
    var similarTo: List<String>
) {
    @Creator
    constructor(definition: String, partOfSpeech: String) : this(
        definition,
        partOfSpeech,
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList()
    )

    fun toDomainWords(word: String): Vocab = Vocab(
        word = word,
        partOfSpeech = Vocab.PartOfSpeech.fromFullWord(partOfSpeech)
            ?: throw Exception("Unseen part of speech [$partOfSpeech]"),
        definition = definition,
        examples = examples,
        synonyms = synonyms
    )
}
