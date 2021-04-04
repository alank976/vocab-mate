package github.io.vocabmate.domain.vocabs

import com.faunadb.client.types.FaunaConstructor
import com.faunadb.client.types.FaunaField
import io.micronaut.core.annotation.Introspected
import java.time.Instant

@Introspected
data class Vocab(
    val id: String? = null,
    val word: String,
    val partOfSpeech: PartOfSpeech,
    val definition: String,
    val examples: List<String> = emptyList(),
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList(),
    val lastUpdated: Instant? = null,
) {

    companion object {
        @JvmStatic
        @Suppress("unused")
        @FaunaConstructor
        fun fromFauna(
            @FaunaField("word") word: String,
            @FaunaField("partOfSpeech") partOfSpeech: String,
            @FaunaField("definition") definition: String,
            @FaunaField("examples") examples: List<String>?,
            @FaunaField("synonyms") synonyms: List<String>?,
            @FaunaField("antonyms") antonyms: List<String>?,
        ) = Vocab(
            word = word,
            partOfSpeech = PartOfSpeech.valueOf(partOfSpeech),
            definition = definition,
            examples = examples ?: emptyList(),
            synonyms = synonyms ?: emptyList(),
            antonyms = antonyms ?: emptyList(),
        )
    }

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


