package github.io.vocabmate.infrastructure

import github.io.vocabmate.domain.words.Word
import github.io.vocabmate.domain.words.WordsService
import github.io.vocabmate.logger
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.uri.UriBuilder
import javax.inject.Named
import javax.inject.Singleton

/**
 * Refer to https://rapidapi.com/dpventures/api/wordsapi/endpoints
 */
@Singleton
@Named("words-api")
class WordsApiClient(
    @Client("\${words-api.url}")
    private val httpClient: RxHttpClient,
    @Value("\${words-api.key-header}")
    private val header: String,
    @Value("\${words-api.key}")
    private val apiKey: String
) : WordsService {
    private val log = logger()

    override fun getWords(value: String): List<Word> {
        val response = invoke(value)
        log.debug("WordsAPI responds $response")
        return response.results.map { it.toDomainWords(response.word) }
    }

    private fun invoke(word: String): WordsResponse {
        val request = UriBuilder.of("/words")
            .path(word)
            .build()
            .let { uri -> HttpRequest.GET<Any>(uri).header(header, apiKey) }
        return httpClient.toBlocking().retrieve(request, WordsResponse::class.java)
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