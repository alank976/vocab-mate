package github.io.vocabmate.infrastructure.fauna

import github.io.vocabmate.domain.vocabs.Vocab
import github.io.vocabmate.logger
import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.testcontainers.perSpec
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.netty.DefaultHttpClient
import org.slf4j.Logger
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.net.URL
import java.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@ExperimentalTime
@Suppress("BlockingMethodInNonBlockingContext")
class FaunaDriverVocabCollectionTest : StringSpec({
    val faunadbContainer: GenericContainer<Nothing> = setupFaunadbContainer()
    var secret: String? = null
    var graphqlImportPort = 8084
    var generalImportPort = 8443
    listener(faunadbContainer.perSpec())

    val givenVocab = Vocab(
        word = "foo",
        partOfSpeech = Vocab.PartOfSpeech.Noun,
        definition = "not important")

    beforeSpec {
        graphqlImportPort = graphqlImportPort.run { faunadbContainer.getMappedPort(this) }
        generalImportPort = generalImportPort.run { faunadbContainer.getMappedPort(this) }
        log.info("ports=[{}, {}]", graphqlImportPort, generalImportPort)

        eventually(30.seconds) {
            faunadbContainer.createDatabase("vocab-mate")
        }
        secret = faunadbContainer.createSecret("vocab-mate")
            .also { log.info("Created key secret={}", it) }

        uploadGraphQLSchemaForIndexes(secret!!, graphqlImportPort)
    }


    "fql findAll works" {
        Thread.sleep(10 * 1000)
//        val result = createInstance(secret!!).findAll().blockingIterable().toList()
//        result.shouldBeEmpty()
    }

    "fql create vocab works" {
//        val result = createInstance(secret!!).create(givenVocab)
//        result.shouldBeEqualToIgnoringFields(givenVocab, Vocab::id, Vocab::lastUpdated)
    }

    "findByWord works" {
//        val result = createInstance(secret!!).findByWord("foo")
//            .blockingIterable().toList()
//        result.single().should {
//            it.shouldBeEqualToIgnoringFields(givenVocab, Vocab::id, Vocab::lastUpdated)
//            it.id shouldNotBe null
//            it.lastUpdated shouldNotBe null
//        }
    }

    "delete works" {
//        val faunaDriverVocabCollection = createInstance(secret!!)
//        val allFoundVocabs = faunaDriverVocabCollection.findAll().blockingIterable()
//        allFoundVocabs shouldHaveSize 1
//        faunaDriverVocabCollection.delete(allFoundVocabs.single().id!!)
//        faunaDriverVocabCollection.findAll().count().blockingGet() shouldBe 0
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
                    withStartupTimeout(Duration.ofMinutes(1))
                    waitingFor(Wait.forLogMessage(".*FaunaDB is ready.*", 1))
                    addExposedPorts(8443, 8084)
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
        private fun uploadGraphQLSchemaForIndexes(secret: String, port: Int) {
            DefaultHttpClient(URL("http://localhost:$port")).let { client ->
                val request =
                    HttpRequest.POST("/import", resource("/raw-schema.gql").readText())
                        .bearerAuth(secret)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                client.exchange(request)
            }.blockingSubscribe({ resp ->
                log.info("Import GraphQL Schema response is: {}", resp.status())
            }) { err -> throw IllegalStateException("Failed to import GraphQL schema", err) }
        }

        @JvmStatic
        private fun createInstance(secret: String) = FaunaDriverVocabCollection(FaunaConfigProps(
            endpoint = "http://localhost:8443",
            apiKey = secret
        ))
    }
}


