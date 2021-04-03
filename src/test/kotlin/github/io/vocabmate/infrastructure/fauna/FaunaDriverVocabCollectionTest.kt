package github.io.vocabmate.infrastructure.fauna

import github.io.vocabmate.domain.vocabs.Vocab
import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe

@Ignored
@Suppress("BlockingMethodInNonBlockingContext")
class FaunaDriverVocabCollectionTest : StringSpec({

    // TODO: testcontainers -> faunadb container...
    val vocabCollection =
        FaunaDriverVocabCollection(FaunaConfigProps(System.getenv("FAUNA_API_KEY")))


    "fql findAll works" {
        val result = vocabCollection.findAll().blockingIterable().toList()
        result.shouldNotBeEmpty()
    }

    "fql create vocab works" {
        val vocab = Vocab(
            word = "foo",
            partOfSpeech = Vocab.PartOfSpeech.Noun,
            definition = "not important")
        vocabCollection.create(vocab) shouldBe vocab
    }

})
