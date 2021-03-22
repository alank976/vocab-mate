package github.io.vocabmate.domain.words

import io.micronaut.core.annotation.Introspected

@Introspected
data class Word(
    val word: String,
    val partOfSpeech: PartOfSpeech,
    val definition: String,
    val examples: List<String> = emptyList(),
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList()
) {

    @Introspected
    enum class PartOfSpeech(val shortForm: String) {
        Noun("n"), Verb("v"), Adjective("adj"), Adverb("adv");

        companion object {
            @JvmStatic
            fun fromFullWord(value: String) = values().find {
                it.name.equals(value, ignoreCase = true)
            }
        }
    }
}


