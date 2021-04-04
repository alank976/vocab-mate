package github.io.vocabmate.infrastructure.fauna

import com.faunadb.client.FaunaClient
import com.faunadb.client.query.Expr
import com.faunadb.client.query.Language.*
import com.faunadb.client.types.FaunaConstructor
import com.faunadb.client.types.FaunaField
import com.faunadb.client.types.Value
import github.io.vocabmate.domain.vocabs.Vocab
import github.io.vocabmate.domain.vocabs.VocabRepository
import io.reactivex.rxjava3.core.Flowable
import java.time.Instant
import javax.inject.Singleton

@Singleton
class FaunaDriverVocabCollection(faunaConfigProps: FaunaConfigProps) : VocabRepository {
    private val client: FaunaClient = FaunaClient.builder()
        .withSecret(faunaConfigProps.apiKey)
        .build()

    override fun findAll(): Flowable<Vocab> = queryVocabsByIndexName("allVocabs")

    override fun findByWord(word: String): Flowable<Vocab> = queryVocabsByIndexName("findVocabsByWord", word)

    override fun create(vocab: Vocab): Vocab =
        fql {
            Create(
                Collection("Vocab"),
                Obj("data", vocab.toFaunaObj())
            )
        }
            .get(VocabResponse::class.java)
            .run { toVocab() }

    private fun queryVocabsByIndexName(indexName: String, vararg words: String): Flowable<Vocab> {
        val paramValue = words.takeIf { it.size > 1 }
            ?.run { Arr(map { word -> Value(word) }) }
            ?: Value(words.first())
        return fql {
            Map(
                Paginate(Match(Index(indexName), paramValue)),
                Lambda("x", Get(Var("x")))
            )
        }
            .at("data")
            .collect(VocabResponse::class.java)
            .map { it.toVocab() }
            .let { Flowable.fromIterable(it) }
    }

    private fun Vocab.toFaunaObj(): Expr =
        mutableMapOf<String, Expr>()
            .apply {
                put("word", Value(word))
                put("partOfSpeech", Value(partOfSpeech.name))
                put("definition", Value(definition))
                examples.takeIf { it.isNotEmpty() }?.let { put("examples", Value(it)) }
                synonyms.takeIf { it.isNotEmpty() }?.let { put("synonyms", Value(it)) }
                antonyms.takeIf { it.isNotEmpty() }?.let { put("antonyms", Value(it)) }
            }
            .let { Obj(it) }

    data class VocabResponse(
        val ref: Value.RefV,
        val ts: Instant,
        val data: Vocab,
    ) {
        companion object {
            @JvmStatic
            @Suppress("unused")
            @FaunaConstructor
            fun fromFauna(
                @FaunaField("ref") ref: Value.RefV,
                @FaunaField("ts") ts: Long,
                @FaunaField("data") data: Vocab,
            ) = VocabResponse(
                ref = ref,
                ts = Instant.ofEpochMilli(ts / 1000),
                data = data
            )
        }

        fun toVocab() = data.run { copy(id = ref.id, lastUpdated = ts) }
    }

    // TODO: make it rx
    private fun fql(f: () -> Expr) = client.query(f()).get()
}