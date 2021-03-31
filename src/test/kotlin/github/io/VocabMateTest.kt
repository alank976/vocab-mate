package github.io

import github.io.vocabmate.domain.vocabs.VocabRepository
import github.io.vocabmate.domain.words.Vocab
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.kotest.annotation.MicronautTest

@MicronautTest
class VocabMateTest(private val application: EmbeddedApplication<*>) : StringSpec({


    "test the server is running" {
        assert(application.isRunning)
    }

    "test repo" {
        val vocabRepository = application.applicationContext.getBean(VocabRepository::class.java)
        val newVocab = Vocab(
            word = "repository",
            partOfSpeech = Vocab.PartOfSpeech.Noun,
            definition = "a place where things are stored and can be found")
        val result = vocabRepository.create(newVocab)
        result shouldBe newVocab
        val findAllResult = vocabRepository.findAll()
        findAllResult.blockingFirst() shouldBe newVocab
    }
})
