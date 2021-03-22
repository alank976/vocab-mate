package github.io.vocabmate.infrastructure

import io.micronaut.core.annotation.Creator
import io.micronaut.core.annotation.Introspected

@Introspected
data class WordsResponse(
    val word: String,
    var results: List<WordsResult>
    // offtopic, syllables and pronunciation
) {
    @Creator
    constructor(word: String) : this(word, emptyList())
}
