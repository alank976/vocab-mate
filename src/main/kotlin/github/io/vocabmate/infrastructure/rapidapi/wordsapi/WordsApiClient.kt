package github.io.vocabmate.infrastructure.rapidapi.wordsapi

import github.io.vocabmate.domain.vocabs.Vocab
import github.io.vocabmate.domain.vocabs.VocabService
import github.io.vocabmate.infrastructure.rapidapi.RapidApiConfigProps
import github.io.vocabmate.logger
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.uri.UriBuilder
import io.reactivex.Flowable
import javax.inject.Named
import javax.inject.Singleton

/**
 * Refer to https://rapidapi.com/dpventures/api/wordsapi/endpoints
 */
@Singleton
@Named("words-api")
class WordsApiClient(
    @Client("\${rapidapi.words-api-url}")
    private val httpClient: RxHttpClient,
    private val rapidApiConfigProps: RapidApiConfigProps
) : VocabService {
    private val log = logger()

    override fun getVocab(value: String): Flowable<Vocab> {
        val response = invoke(value)
        log.debug("WordsAPI responds $response")
        return response.flatMap { wordsResponse ->
            Flowable.fromIterable(wordsResponse.results).map {
                it.toDomainWords(wordsResponse.word)
            }
        }
    }

    private fun invoke(word: String): Flowable<WordsResponse> {
        val request = UriBuilder.of("/words")
            .path(word)
            .build()
            .let { uri ->
                HttpRequest.GET<Any>(uri).header(rapidApiConfigProps.apiKeyHeader, rapidApiConfigProps.apiKey)
            }
        return httpClient.exchange(request, WordsResponse::class.java)
            .map { response ->
                response.header("X-RateLimit-requests-Remaining")
                    ?.toInt()
                    ?.let { limit ->
                        log.info("API usage limit remaining={}", limit)
                        if (limit < 10) {
                            log.error("MUST STOP USING THE API FOR THE SAKE OF $$$$$")
                        }
                    }
                response.body()!!
            }
    }
}