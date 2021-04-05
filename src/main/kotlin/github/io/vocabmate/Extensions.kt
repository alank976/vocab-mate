package github.io.vocabmate

import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

fun Any.logger() = LoggerFactory.getLogger(this::class.java)

fun logger(clazz: KClass<*>) = LoggerFactory.getLogger(clazz.java)