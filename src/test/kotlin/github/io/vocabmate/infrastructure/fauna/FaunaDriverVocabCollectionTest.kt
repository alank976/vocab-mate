package github.io.vocabmate.infrastructure.fauna

import github.io.vocabmate.domain.vocabs.Vocab
import github.io.vocabmate.logger
import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.testcontainers.perSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.netty.DefaultHttpClient
import org.slf4j.Logger
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.net.URL
import kotlin.time.ExperimentalTime
import kotlin.time.minutes
import kotlin.time.seconds

@ExperimentalTime
@Suppress("BlockingMethodInNonBlockingContext")
class FaunaDriverVocabCollectionTest : StringSpec({
    var secret: String? = null
    val faunadbContainer: GenericContainer<Nothing> = setupFaunadbContainer()
    listener(faunadbContainer.perSpec())

    val givenVocab = Vocab(
        word = "foo",
        partOfSpeech = Vocab.PartOfSpeech.Noun,
        definition = "not important")

    beforeSpec {
        eventually(5.minutes) {
            faunadbContainer.createDatabase("vocab-mate")
        }
        secret = faunadbContainer.createSecret("vocab-mate")
            .also { log.info("Created key secret={}", it) }

        uploadGraphQLSchemaForIndexes(secret!!)
    }


    "fql findAll works" {
        val result = createInstance(secret!!).findAll().blockingIterable().toList()
        result.shouldBeEmpty()
    }

    "fql create vocab works" {
        val result = createInstance(secret!!).create(givenVocab)
        result.shouldBeEqualToIgnoringFields(givenVocab, Vocab::id, Vocab::lastUpdated)
    }

    "findByWord works" {
        val result = createInstance(secret!!).findByWord("foo")
            .blockingIterable().toList()
        result.single().should {
            it.shouldBeEqualToIgnoringFields(givenVocab, Vocab::id, Vocab::lastUpdated)
            it.id shouldNotBe null
            it.lastUpdated shouldNotBe null
        }
    }

    "delete works" {
        val faunaDriverVocabCollection = createInstance(secret!!)
        val allFoundVocabs = faunaDriverVocabCollection.findAll().blockingIterable()
        allFoundVocabs shouldHaveSize 1
        faunaDriverVocabCollection.delete(allFoundVocabs.single().id!!)
        faunaDriverVocabCollection.findAll().count().blockingGet() shouldBe 0
    }
}) {
    companion object {
        val log: Logger = logger(FaunaDriverVocabCollectionTest::class)

        @JvmStatic
        private fun resource(name: String): URL = this::class.java.getResource(name)

        @JvmStatic
        private fun setupFaunadbContainer(): GenericContainer<Nothing> =
            GenericContainer<Nothing>("fauna/faunadb:latest")
                .apply {
                    waitingFor(Wait.forLogMessage(".*FaunaDB is ready.*", 1))
                    portBindings = listOf("8443:8443", "8084:8084")
                }

        @JvmStatic
        private fun GenericContainer<Nothing>.createDatabase(name: String) {
            execInContainer("fauna", "create-database", name)
                .stderr
                .takeIf { it.isNotEmpty() }
                ?.let { throw IllegalStateException("Failed to create DB [$name]. Error=$it") }
        }

        @JvmStatic
        private fun GenericContainer<Nothing>.createSecret(dbName: String) = run {
            val result = execInContainer("fauna", "create-key", dbName)
            result.stderr
                .takeIf { it.isNotEmpty() }
                ?.let { throw IllegalStateException("Failed to create secret for DB $dbName. Error=$it") }

            result.stdout
                .let { stdout ->
                    Regex(".*secret: (.{40}).*").find(stdout)?.groupValues?.find { it.length == 40 }
                }
                ?: throw IllegalStateException("Failed to create backend secret")
        }

        @JvmStatic
        private fun uploadGraphQLSchemaForIndexes(secret: String) {
            DefaultHttpClient(URL("http://localhost:8084")).let { client ->
                val request =
                    HttpRequest.POST("/import", FaunaDriverVocabCollectionTest.resource("/raw-schema.gql").readText())
                        .bearerAuth(secret)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                client.exchange(request)
            }.blockingSubscribe({ resp ->
                FaunaDriverVocabCollectionTest.log.info("Import GraphQL Schema response is: {}", resp.status())
            }) { err -> throw IllegalStateException("Failed to import GraphQL schema", err) }
        }

        @JvmStatic
        private fun createInstance(secret: String) = FaunaDriverVocabCollection(FaunaConfigProps(
            endpoint = "http://localhost:8443",
            apiKey = secret
        ))
    }
}


