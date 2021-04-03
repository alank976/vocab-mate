package github.io.vocabmate.infrastructure.rapidapi

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("rapidapi")
data class RapidApiConfigProps(
    var apiKeyHeader: String? = null,
    var apiKey: String? = null
)