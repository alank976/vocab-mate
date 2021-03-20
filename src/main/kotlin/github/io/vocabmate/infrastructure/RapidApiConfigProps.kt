package github.io.vocabmate.infrastructure

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("rapid-api")
data class RapidApiConfigProps(
    var apiKeyHeader: String? = null,
    var apiKey: String? = null
)
