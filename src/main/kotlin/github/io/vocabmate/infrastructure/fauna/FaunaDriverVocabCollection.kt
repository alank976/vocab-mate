package github.io.vocabmate.infrastructure.fauna

import com.faunadb.client.FaunaClient
import com.faunadb.client.query.Expr
import com.faunadb.client.query.Language.*
import com.faunadb.client.types.FaunaConstructor
import com.faunadb.client.types.FaunaField
import com.faunadb.client.types.Value
import github.io.vocabmate.domain.vocabs.Vocab
import github.io.vocabmate.domain.vocabs.VocabRepository
import github.io.vocabmate.logger
import io.reactivex.rxjava3.core.Flowable
import java.time.Instant


class FaunaDriverVocabCollection(faunaConfigProps: FaunaConfigProps) : VocabRepository {
    private val log = logger()
    private val client: FaunaClient = FaunaClient.builder()
        .withSecret(faunaConfigProps.apiKey)
        .build()

    override fun findAll(): Flowable<Vocab> {
        return fql {
            Map(
                Paginate(Match(Index("allVocabs"))),
                Lambda("x", Get(Var("x")))
            )
        }
            .at("data")
            .collect(VocabResponse::class.java)
            .map {
                log.info("found vocab ID={}", it.ref.id)
                it.data
            }
            .let { Flowable.fromIterable(it) }
    }

    override fun create(vocab: Vocab): Vocab {
        return fql {
            Create(
                Collection("Vocab"),
                Obj("data", vocab.toFaunaObj())
            )
        }
            .get(VocabResponse::class.java)
            .let {
                log.info("created vocab ID={}", it.ref.id)
                it.data
            }
    }

    private fun Vocab.toFaunaObj(): Expr {
        val map: Map<String, Expr> = mutableMapOf<String, Expr>()
            .apply {
                put("word", Value(word))
                put("partOfSpeech", Value(partOfSpeech.name))
                put("definition", Value(definition))
                examples.takeIf { it.isNotEmpty() }?.let { put("examples", Value(it)) }
                synonyms.takeIf { it.isNotEmpty() }?.let { put("synonyms", Value(it)) }
                antonyms.takeIf { it.isNotEmpty() }?.let { put("antonyms", Value(it)) }
            }
        return Obj(map)
    }

    data class VocabResponse(
        val ref: Value.RefV,
        val ts: Instant,
        val data: Vocab,
    ) {
        companion object {
            @JvmStatic
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
    }

    private fun fql(f: () -> Expr) = client.query(f()).get()
}