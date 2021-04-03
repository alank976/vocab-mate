package github.io.vocabmate.domain.vocabs

import io.reactivex.rxjava3.core.Flowable

interface VocabRepository {
    fun findAll(): Flowable<Vocab>
    fun findByWord(word: String): Flowable<Vocab>
    fun create(vocab: Vocab): Vocab
}