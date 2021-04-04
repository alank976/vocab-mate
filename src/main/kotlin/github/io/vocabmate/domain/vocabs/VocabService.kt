package github.io.vocabmate.domain.vocabs

import io.reactivex.rxjava3.core.Flowable
import java.time.Instant
import javax.inject.Named
import javax.inject.Singleton

@Named("cached")
@Singleton
class VocabService(
    private val vocabConfigProps: VocabConfigProps,
    private val vocabRepository: VocabRepository,
    @Named("words-api") private val dictionaryService: DictionaryService,
) {
    fun getVocab(vocab: String): Flowable<Vocab> {
        val oldestAcceptableTime = Instant.now().minus(vocabConfigProps.expiry)
        return vocabRepository.findByWord(vocab)
            // empty or any of vocab is outdated => empty; else collected vocabs to list
            .reduce((false to mutableListOf<Vocab>())) { (anyOutDated, acc), v ->
                val isVocabOutDated = v.lastUpdated?.isBefore(oldestAcceptableTime) ?: true
                val isOutDated = anyOutDated || isVocabOutDated
                isOutDated to acc.apply {
                    if (!isOutDated) {
                        add(v)
                    }
                }
            }
            .flattenAsFlowable { (anyOutDated, accumulatedVocabs) ->
                if (anyOutDated) emptyList() else accumulatedVocabs
            }
            .switchIfEmpty(
                vocabRepository.findByWord(vocab)
                    .filter {
                        it.run {
                            vocabRepository.delete(id!!)
                            false
                        }
                    }
                    .switchIfEmpty(dictionaryService.getVocab(vocab)
                        .map {
                            vocabRepository.create(it)
                        }
                    )
            )
    }
}