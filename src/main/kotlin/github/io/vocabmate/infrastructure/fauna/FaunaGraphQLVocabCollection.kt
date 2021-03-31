package github.io.vocabmate.infrastructure.fauna

import com.expediagroup.graphql.client.ktor.GraphQLKtorClient
import com.expediagroup.graphql.client.types.GraphQLClientRequest
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import github.io.vocabmate.domain.vocabs.VocabRepository
import github.io.vocabmate.fauna.graphql.generated.AllVocabsQuery
import github.io.vocabmate.fauna.graphql.generated.CreateVocabMutation
import github.io.vocabmate.fauna.graphql.generated.allvocabsquery.VocabPage
import github.io.vocabmate.fauna.graphql.generated.inputs.VocabInput
import io.ktor.client.request.*
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.runBlocking
import java.net.URL
import javax.annotation.PreDestroy
import javax.inject.Singleton
import github.io.vocabmate.domain.words.Vocab as VocabDomain
import github.io.vocabmate.fauna.graphql.generated.allvocabsquery.Vocab as VocabQuery
import github.io.vocabmate.fauna.graphql.generated.createvocabmutation.Vocab as VocabMutation
import github.io.vocabmate.fauna.graphql.generated.enums.PartOfSpeech as GraphQLPartOfSpeech

@Singleton
class FaunaGraphQLVocabCollection(private val faunaConfigProps: FaunaConfigProps) : VocabRepository, AutoCloseable {
    private val ktorClient = GraphQLKtorClient(url = URL(faunaConfigProps.graphqlUrl))

    override fun findAll(): Flowable<VocabDomain> {
        // TODO: revisit and make coroutine to RX
        val vocabPage: VocabPage = runBlocking {
            val result = ktorClient.executeWithApiKeyHeader(AllVocabsQuery())
            result.data?.allVocabs!!
        }
        return vocabPage.data
                .mapNotNull { it?.toDomain() }
                .let { Flowable.fromIterable(it) }
    }

    override fun create(vocab: VocabDomain): VocabDomain {
        // TODO: revisit and make coroutine to RX
        return runBlocking {
            val result = ktorClient.executeWithApiKeyHeader(CreateVocabMutation(vocab.toMutationModel()))
            result.data?.createVocab!!
        }.toDomain()
    }

    @PreDestroy
    @Throws(Exception::class)
    override fun close() {
        ktorClient.close()
    }


    private suspend fun <T : Any> GraphQLKtorClient.executeWithApiKeyHeader(request: GraphQLClientRequest<T>): GraphQLClientResponse<T> =
            execute(request) {
                headers {
                    append("Authorization", "bearer ${faunaConfigProps.apiKey}")
                }
            }


    private fun VocabQuery.toDomain(): VocabDomain = VocabDomain(
            word = word,
            partOfSpeech = VocabDomain.PartOfSpeech.fromFullWord(partOfSpeech.name)!!,
            definition = definition,
            examples = examples ?: emptyList(),
            synonyms = synonyms ?: emptyList(),
            antonyms = antonyms ?: emptyList()
    )

    private fun VocabMutation.toDomain() = VocabDomain(
            word = word,
            partOfSpeech = VocabDomain.PartOfSpeech.fromFullWord(partOfSpeech.name)!!,
            definition = definition,
            examples = examples ?: emptyList(),
            synonyms = synonyms ?: emptyList(),
            antonyms = antonyms ?: emptyList()
    )

    private fun VocabDomain.toMutationModel() = CreateVocabMutation.Variables(
            VocabInput(
                    word = word,
                    partOfSpeech = GraphQLPartOfSpeech.valueOf(partOfSpeech.name.toUpperCase()),
                    definition = definition,
                    examples = examples,
                    synonyms = synonyms,
                    antonyms = antonyms
            )
    )
}

