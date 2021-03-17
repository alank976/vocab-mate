package github.io.vocabmate.infrastructure

import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.uri.UriBuilder
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class UrbanDictClient(
    @Client("\${urban-dict.url}")
    private val httpClient: RxHttpClient,
    @Value("\${urban-dict.api-key-header}")
    private val apiKeyHeader: String,
    @Value("\${urban-dict.api-key}")
    private val apiKeyValue: String
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun checkDict(word: String): MutableIterable<Any>? {
        val request = UriBuilder.of("/define")
            .queryParam("term", word)
            .build()
            .let { uri -> HttpRequest.GET<Any>(uri).header(apiKeyHeader, apiKeyValue) }

        val result = httpClient.retrieve(request, Any::class.java)
        val blockedResult = result.blockingIterable()
        log.info("result = {}", blockedResult)
        return blockedResult
    }
}
