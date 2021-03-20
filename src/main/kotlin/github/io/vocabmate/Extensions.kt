package github.io.vocabmate

import org.slf4j.LoggerFactory

fun Any.logger() = LoggerFactory.getLogger(this::class.java)