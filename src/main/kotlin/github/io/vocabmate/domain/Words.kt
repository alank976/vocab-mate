package github.io.vocabmate.domain

data class Words(
    val value: String,
    val partOfSpeech: PartOfSpeech,
    val definition: String,
    val examples: List<String> = emptyList(),
    val synonyms: List<Words> = emptyList(),
    val antonyms: List<Words> = emptyList()
) {
    enum class PartOfSpeech(val shortForm: String) {
        Noun("n"), Verb("v"), Adjective("adj"), Adverb("adv")
    }
}


