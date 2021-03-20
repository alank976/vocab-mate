package github.io.vocabmate.domain.words

data class Word(
    val word: String,
    val partOfSpeech: PartOfSpeech,
    val definition: String,
    val examples: List<String> = emptyList(),
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList()
) {
    enum class PartOfSpeech(val shortForm: String) {
        Noun("n"), Verb("v"), Adjective("adj"), Adverb("adv");

        companion object {
            fun fromFullWord(value: String) = values().find {
                it.name.equals(value, ignoreCase = true)
            }
        }
    }
}


