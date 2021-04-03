package github.io.vocabmate.infrastructure.rapidapi.urbandict

import com.fasterxml.jackson.annotation.JsonProperty
import github.io.vocabmate.domain.vocabs.DictionaryService
import github.io.vocabmate.domain.vocabs.Vocab
import github.io.vocabmate.logger
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.uri.UriBuilder
import io.reactivex.rxjava3.core.Flowable
import java.time.LocalDateTime
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named("urban")
class UrbanDictClient(
    @Client("\${rapid-api.urban-dict-url}")
    private val httpClient: RxHttpClient,
    private val rapidApiConfigProps: github.io.vocabmate.infrastructure.rapidapi.RapidApiConfigProps,
) : DictionaryService {
    private val log = logger()

    override fun getVocab(vocab: String): Flowable<Vocab> {
        val response = checkDict(vocab)
        TODO("on hold due to mistmatch domain model too much")
    }

    private fun checkDict(term: String): UrbanDictResponse? {
        val request = UriBuilder.of("/define")
            .queryParam("term", term)
            .build()
            .let { uri ->
                HttpRequest.GET<Any>(uri).header(rapidApiConfigProps.apiKeyHeader, rapidApiConfigProps.apiKey)
            }

        val result = httpClient.toBlocking().retrieve(request, UrbanDictResponse::class.java)
        log.info("result = {}", result)
        return result
    }

    data class UrbanDictResponse(val list: List<UrbanDictDefinition>)

    data class UrbanDictDefinition(
        val definition: String,
        val permalink: String,
        @JsonProperty("thumbs_up")
        val thumbsUp: Int,
        @JsonProperty("sound_urls")
        val soundUrls: List<String> = emptyList(),
        val author: String,
        val word: String,
        @JsonProperty("defid")
        val defId: Long,
        @JsonProperty("current_vote")
        val currentVote: String,
        @JsonProperty("written_on")
        val writtenOn: LocalDateTime,
        val example: String,
        @JsonProperty("thumbs_down")
        val thumbsDown: Int,
    )
}
