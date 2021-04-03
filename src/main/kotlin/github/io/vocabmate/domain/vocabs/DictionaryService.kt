package github.io.vocabmate.domain.vocabs

import io.reactivex.rxjava3.core.Flowable

interface DictionaryService {
    fun getVocab(vocab: String): Flowable<Vocab>
}