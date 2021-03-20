package github.io.vocabmate.infrastructure

import github.io.vocabmate.domain.words.Word
import github.io.vocabmate.domain.words.WordsService
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
    @Client("\${rapid-api.words-api-url}")
    private val httpClient: RxHttpClient,
    private val rapidApiConfigProps: RapidApiConfigProps
) : WordsService {
    private val log = logger()

    override fun getWords(value: String): Flowable<Word> {
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


    data class WordsResponse(
        val word: String,
        val results: List<WordsResult> = emptyList()
        // offtopic, syllables and pronunciation
    )

    data class WordsResult(
        val definition: String,
        val partOfSpeech: String,
        val synonyms: List<String> = emptyList(),
        val typeOf: List<String> = emptyList(),
        val hasTypes: List<String> = emptyList(),
        val derivation: List<String> = emptyList(),
        val examples: List<String> = emptyList(),
        val similarTo: List<String> = emptyList(),
    ) {
        fun toDomainWords(word: String): Word = Word(
            word = word,
            partOfSpeech = Word.PartOfSpeech.fromFullWord(partOfSpeech)
                ?: throw Exception("Unseen part of speech [$partOfSpeech]"),
            definition = definition,
            examples = examples,
            synonyms = synonyms
        )
    }
}