package github.io.vocabmate.domain.vocabs

import io.reactivex.Flowable

interface VocabService {
    fun getVocab(value: String): Flowable<Vocab>
}