package github.io.vocabmate.domain.vocabs

import io.micronaut.context.annotation.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("vocab")
data class VocabConfigProps(var expiry: Duration? = null)