package github.io.vocabmate.domain.vocabs

import io.reactivex.rxjava3.core.Flowable
import javax.inject.Named
import javax.inject.Singleton

@Named("cached")
@Singleton
class VocabService(
    private val vocabRepository: VocabRepository,
    @Named("words-api") private val dictionaryService: DictionaryService,
) {
    fun getVocab(vocab: String): Flowable<Vocab> {
        return vocabRepository.findByWord(vocab)
            // empty or any of vocab is outdated => empty; else collected vocabs to list
            .reduce((false to mutableListOf<Vocab>())) { (anyOutDated, acc), v ->
                // TODO: replace v.is-up-to-dated()
                val isOutDated = anyOutDated || true
                isOutDated to acc.apply {
                    if (!isOutDated) {
                        add(v)
                    }
                }
            }
            .flattenAsFlowable { (anyOutDated, accumulatedVocabs) ->
                if (anyOutDated) emptyList() else accumulatedVocabs
            }
            .switchIfEmpty(dictionaryService.getVocab(vocab))
    }
}