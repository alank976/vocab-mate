package github.io.vocabmate.domain.vocabs

import io.kotest.core.spec.style.StringSpec
import io.mockk.*
import io.reactivex.rxjava3.core.Flowable
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit


class VocabServiceTest : StringSpec({
    val repo: VocabRepository = mockk()
    val dictService: DictionaryService = mockk()
    val config = VocabConfigProps(Duration.ofDays(1))
    val vocabService = VocabService(config, repo, dictService)

    val now = Instant.now()
    val givenDictVocab = Vocab(
        word = "foo",
        partOfSpeech = Vocab.PartOfSpeech.Noun,
        definition = "new meaning",
    )

    afterTest {
        clearMocks(repo, dictService)
    }

    "get vocab when existing vocabs found and they are up-to-dated" {
        val storedVocab = Vocab(
            id = "123",
            word = "foo",
            partOfSpeech = Vocab.PartOfSpeech.Noun,
            definition = "bar",
            lastUpdated = Instant.now().minus(2, ChronoUnit.MINUTES)
        )
        every { repo.findByWord("foo") } returns Flowable.just(storedVocab)
        every { dictService.getVocab("foo") } returns Flowable.just(givenDictVocab)

        vocabService.getVocab("foo")
            .test()
            .assertComplete()
            .assertResult(storedVocab)

        verify(exactly = 0) { repo.create(any()) }
    }

    "get vocab when no existing vocab stored" {
        every { repo.findByWord("foo") } returns Flowable.empty()
        every { repo.create(any()) } answers {
            val input: Vocab = arg(0)
            input.copy(id = "123", lastUpdated = now)
        }
        every { dictService.getVocab("foo") } returns Flowable.just(givenDictVocab)

        vocabService.getVocab("foo")
            .test()
            .assertComplete()
            .assertValues(givenDictVocab.copy(id = "123", lastUpdated = now))

        verify(exactly = 0) { repo.delete(any()) }
        verify(exactly = 1) { repo.create(any()) }
    }


    "get vocab when one of definitions stored is outdated" {
        val storedVocab = Vocab(
            id = "123",
            word = "foo",
            partOfSpeech = Vocab.PartOfSpeech.Noun,
            definition = "bar",
            lastUpdated = Instant.now().minus(10, ChronoUnit.DAYS)
        )
        every { repo.findByWord("foo") } returns Flowable.just(storedVocab)
        every { repo.create(any()) } answers {
            val input: Vocab = arg(0)
            input.copy(id = "123", lastUpdated = now)
        }
        every { repo.delete(any()) } just runs
        every { dictService.getVocab("foo") } returns Flowable.just(givenDictVocab)

        vocabService.getVocab("foo")
            .test()
            .assertComplete()
            .assertValues(givenDictVocab.copy(id = "123", lastUpdated = now))

        verify(exactly = 1) { repo.delete(any()) }
        verify(exactly = 1) { repo.create(givenDictVocab) }
    }
})
