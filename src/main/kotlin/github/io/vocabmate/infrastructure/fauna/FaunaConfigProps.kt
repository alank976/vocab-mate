package github.io.vocabmate.infrastructure.fauna

import io.micronaut.context.annotation.ConfigurationProperties


@ConfigurationProperties("fauna")
data class FaunaConfigProps(
    var graphqlUrl: String? = null,
    var apiKey: String? = null
)
