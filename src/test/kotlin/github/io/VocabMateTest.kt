package github.io

import github.io.vocabmate.domain.vocabs.Vocab
import github.io.vocabmate.domain.vocabs.VocabRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.kotest.annotation.MicronautTest

@MicronautTest
class VocabMateTest(private val application: EmbeddedApplication<*>) : StringSpec({
    val vocabRepository = application.applicationContext.getBean(VocabRepository::class.java)
    val newVocab = Vocab(
        word = "test-phrase",
        partOfSpeech = Vocab.PartOfSpeech.Noun,
        definition = "Just a test phrase")

    "test the server is running" {
        assert(application.isRunning)
    }

    "test fauna graphql repo impl works" {
        val result = vocabRepository.create(newVocab)
        result.shouldBeEqualToIgnoringFields(newVocab, Vocab::id, Vocab::lastUpdated)
        vocabRepository.findByWord(newVocab.word)
            .test()
            .assertComplete()
            .assertValue { v ->
                v.word == newVocab.word &&
                        v.partOfSpeech == newVocab.partOfSpeech &&
                        v.definition == newVocab.definition
            }
    }
    finalizeSpec {
        vocabRepository.findByWord(newVocab.word)
            .subscribe {
                vocabRepository.delete(it.id!!)
            }
    }
})
