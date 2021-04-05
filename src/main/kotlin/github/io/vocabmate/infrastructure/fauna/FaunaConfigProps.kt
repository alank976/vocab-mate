package github.io.vocabmate.infrastructure.fauna

import io.micronaut.context.annotation.ConfigurationProperties


@ConfigurationProperties("fauna")
data class FaunaConfigProps(
    var endpoint: String? = null,
    var apiKey: String? = null
)
